package com.ucar.datalink.worker.core.runtime.coordinate;

import com.ucar.datalink.common.DatalinkProtocol;

import java.util.Collection;

/**
 * Listener for rebalance events in the worker group.
 *
 * @author lubiao
 */
public interface WorkerRebalanceListener {
    /**
     * Invoked when a new assignment is created by joining the Datalink worker group. This is invoked for both successful
     * and unsuccessful assignments.
     */
    void onAssigned(DatalinkProtocol.Assignment assignment, int generation);

    /**
     * Invoked when a rebalance operation starts, revoking ownership for the set of jobs and tasks.
     */
    void onRevoked(String leader, Collection<String> tasks);
}
