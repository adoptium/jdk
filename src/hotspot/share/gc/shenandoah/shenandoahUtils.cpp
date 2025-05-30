/*
 * Copyright (c) 2017, 2021, Red Hat, Inc. All rights reserved.
 * Copyright Amazon.com Inc. or its affiliates. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */


#include "gc/shared/gcCause.hpp"
#include "gc/shared/gcTrace.hpp"
#include "gc/shared/gcWhen.hpp"
#include "gc/shared/referenceProcessorStats.hpp"
#include "gc/shenandoah/heuristics/shenandoahHeuristics.hpp"
#include "gc/shenandoah/shenandoahCollectorPolicy.hpp"
#include "gc/shenandoah/shenandoahHeap.inline.hpp"
#include "gc/shenandoah/shenandoahOldGeneration.hpp"
#include "gc/shenandoah/shenandoahReferenceProcessor.hpp"
#include "gc/shenandoah/shenandoahUtils.hpp"
#include "gc/shenandoah/shenandoahYoungGeneration.hpp"
#include "jfr/jfrEvents.hpp"
#include "utilities/debug.hpp"

ShenandoahPhaseTimings::Phase ShenandoahTimingsTracker::_current_phase = ShenandoahPhaseTimings::_invalid_phase;

ShenandoahGCSession::ShenandoahGCSession(GCCause::Cause cause, ShenandoahGeneration* generation) :
  _heap(ShenandoahHeap::heap()),
  _generation(generation),
  _timer(_heap->gc_timer()),
  _tracer(_heap->tracer()) {
  assert(!ShenandoahGCPhase::is_current_phase_valid(), "No current GC phase");

  _heap->on_cycle_start(cause, _generation);

  _timer->register_gc_start();
  _tracer->report_gc_start(cause, _timer->gc_start());
  _heap->trace_heap_before_gc(_tracer);

  _trace_cycle.initialize(_heap->cycle_memory_manager(), cause,
          "end of GC cycle",
          /* allMemoryPoolsAffected */    true,
          /* recordGCBeginTime = */       true,
          /* recordPreGCUsage = */        true,
          /* recordPeakUsage = */         true,
          /* recordPostGCUsage = */       true,
          /* recordAccumulatedGCTime = */ true,
          /* recordGCEndTime = */         true,
          /* countCollection = */         true
  );
}

ShenandoahGCSession::~ShenandoahGCSession() {
  _heap->on_cycle_end(_generation);
  _timer->register_gc_end();
  _heap->trace_heap_after_gc(_tracer);
  _tracer->report_gc_reference_stats(_generation->ref_processor()->reference_process_stats());
  _tracer->report_gc_end(_timer->gc_end(), _timer->time_partitions());
  assert(!ShenandoahGCPhase::is_current_phase_valid(), "No current GC phase");
}

ShenandoahGCPauseMark::ShenandoahGCPauseMark(uint gc_id, const char* notification_message, SvcGCMarker::reason_type type) :
  _heap(ShenandoahHeap::heap()), _gc_id_mark(gc_id), _svc_gc_mark(type), _is_gc_active_mark() {
  _trace_pause.initialize(_heap->stw_memory_manager(), _heap->gc_cause(),
          notification_message,
          /* allMemoryPoolsAffected */    true,
          /* recordGCBeginTime = */       true,
          /* recordPreGCUsage = */        false,
          /* recordPeakUsage = */         false,
          /* recordPostGCUsage = */       false,
          /* recordAccumulatedGCTime = */ true,
          /* recordGCEndTime = */         true,
          /* countCollection = */         true
  );
}

ShenandoahPausePhase::ShenandoahPausePhase(const char* title, ShenandoahPhaseTimings::Phase phase, bool log_heap_usage) :
  ShenandoahTimingsTracker(phase),
  _tracer(title, nullptr, GCCause::_no_gc, log_heap_usage),
  _timer(ShenandoahHeap::heap()->gc_timer()) {
  _timer->register_gc_pause_start(title);
}

ShenandoahPausePhase::~ShenandoahPausePhase() {
  _timer->register_gc_pause_end();
}

ShenandoahConcurrentPhase::ShenandoahConcurrentPhase(const char* title, ShenandoahPhaseTimings::Phase phase, bool log_heap_usage) :
  ShenandoahTimingsTracker(phase),
  _tracer(title, nullptr, GCCause::_no_gc, log_heap_usage),
  _timer(ShenandoahHeap::heap()->gc_timer()) {
  _timer->register_gc_concurrent_start(title);
}

ShenandoahConcurrentPhase::~ShenandoahConcurrentPhase() {
  _timer->register_gc_concurrent_end();
}

ShenandoahTimingsTracker::ShenandoahTimingsTracker(ShenandoahPhaseTimings::Phase phase, bool should_aggregate) :
  _timings(ShenandoahHeap::heap()->phase_timings()), _phase(phase), _should_aggregate(should_aggregate) {
  assert(Thread::current()->is_VM_thread() || Thread::current()->is_ConcurrentGC_thread(),
          "Must be set by these threads");
  _parent_phase = _current_phase;
  _current_phase = phase;
  _start = os::elapsedTime();
}

ShenandoahTimingsTracker::~ShenandoahTimingsTracker() {
  _timings->record_phase_time(_phase, os::elapsedTime() - _start, _should_aggregate);
  _current_phase = _parent_phase;
}

bool ShenandoahTimingsTracker::is_current_phase_valid() {
  return _current_phase < ShenandoahPhaseTimings::_num_phases;
}

ShenandoahGCPhase::ShenandoahGCPhase(ShenandoahPhaseTimings::Phase phase) :
  ShenandoahTimingsTracker(phase),
  _timer(ShenandoahHeap::heap()->gc_timer()) {
  _timer->register_gc_phase_start(ShenandoahPhaseTimings::phase_name(phase), Ticks::now());
}

ShenandoahGCPhase::~ShenandoahGCPhase() {
  _timer->register_gc_phase_end(Ticks::now());
}

ShenandoahGCWorkerPhase::ShenandoahGCWorkerPhase(const ShenandoahPhaseTimings::Phase phase) :
    _timings(ShenandoahHeap::heap()->phase_timings()), _phase(phase) {
  _timings->record_workers_start(_phase);
}

ShenandoahGCWorkerPhase::~ShenandoahGCWorkerPhase() {
  _timings->record_workers_end(_phase);
}

ShenandoahWorkerSession::ShenandoahWorkerSession(uint worker_id) {
  assert(worker_id == WorkerThread::worker_id(), "Wrong worker id");
}

ShenandoahConcurrentWorkerSession::~ShenandoahConcurrentWorkerSession() {
  _event.commit(GCId::current(), ShenandoahPhaseTimings::phase_name(ShenandoahGCPhase::current_phase()));
}

ShenandoahParallelWorkerSession::~ShenandoahParallelWorkerSession() {
  _event.commit(GCId::current(), WorkerThread::worker_id(), ShenandoahPhaseTimings::phase_name(ShenandoahGCPhase::current_phase()));
}
