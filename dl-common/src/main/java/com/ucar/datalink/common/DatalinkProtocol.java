package com.ucar.datalink.common;

import org.apache.kafka.common.protocol.types.*;


import java.nio.ByteBuffer;
import java.util.*;


/**
 * This class implements the protocol for Datalink workers in a group. It includes the format of worker state used when
 * joining the group and distributing assignments, and the format of assignments of jobs and tasks to workers.
 */
public class DatalinkProtocol {
    public static final String VERSION_KEY_NAME = "version";
    public static final String URL_KEY_NAME = "url";
    public static final String CONFIG_VERSION_KEY_NAME = "config-version";
    public static final String LEADER_KEY_NAME = "leader";
    public static final String LEADER_URL_KEY_NAME = "leader-url";
    public static final String ERROR_KEY_NAME = "error";
    public static final String TASKS_KEY_NAME = "tasks";
    public static final String ASSIGNMENT_KEY_NAME = "assignment";

    public static final short DATALINK_PROTOCOL_V0 = 0;
    public static final Schema DATALINK_PROTOCOL_HEADER_SCHEMA = new Schema(
            new Field(VERSION_KEY_NAME, Type.INT16));
    private static final Struct DATALINK_PROTOCOL_HEADER_V0 = new Struct(DATALINK_PROTOCOL_HEADER_SCHEMA)
            .set(VERSION_KEY_NAME, DATALINK_PROTOCOL_V0);

    public static final Schema CONFIG_STATE_V0 = new Schema(
            new Field(URL_KEY_NAME, Type.STRING),
            new Field(CONFIG_VERSION_KEY_NAME, Type.INT64));

    // Assignments for each worker are a set of jobs and tasks. These are categorized by connector ID. A sentinel
    // task ID (TASK) is used to indicate the connector itself (i.e. that the assignment includes
    // responsibility for running the Connector instance in addition to any tasks it generates).
    public static final Schema DATALINK_ASSIGNMENT_V0 = new Schema(
            new Field(TASKS_KEY_NAME, new ArrayOf(Type.STRING)));
    public static final Schema ASSIGNMENT_V0 = new Schema(
            new Field(ERROR_KEY_NAME, Type.INT16),
            new Field(LEADER_KEY_NAME, Type.STRING),
            new Field(LEADER_URL_KEY_NAME, Type.STRING),
            new Field(CONFIG_VERSION_KEY_NAME, Type.INT64),
            new Field(ASSIGNMENT_KEY_NAME, DATALINK_ASSIGNMENT_V0));

    public static ByteBuffer serializeMetadata(WorkerState workerState) {
        Struct struct = new Struct(CONFIG_STATE_V0);
        struct.set(URL_KEY_NAME, workerState.url());
        struct.set(CONFIG_VERSION_KEY_NAME, workerState.version());
        ByteBuffer buffer = ByteBuffer.allocate(DATALINK_PROTOCOL_HEADER_V0.sizeOf() + CONFIG_STATE_V0.sizeOf(struct));
        DATALINK_PROTOCOL_HEADER_V0.writeTo(buffer);
        CONFIG_STATE_V0.write(buffer, struct);
        buffer.flip();
        return buffer;
    }

    public static WorkerState deserializeMetadata(ByteBuffer buffer) {
        Struct header = DATALINK_PROTOCOL_HEADER_SCHEMA.read(buffer);
        Short version = header.getShort(VERSION_KEY_NAME);
        checkVersionCompatibility(version);
        Struct struct = CONFIG_STATE_V0.read(buffer);
        long configOffset = struct.getLong(CONFIG_VERSION_KEY_NAME);
        String url = struct.getString(URL_KEY_NAME);
        return new WorkerState(url, configOffset);
    }

    public static ByteBuffer serializeAssignment(Assignment assignment) {
        Struct struct = new Struct(ASSIGNMENT_V0);
        struct.set(ERROR_KEY_NAME, assignment.error());
        struct.set(LEADER_KEY_NAME, assignment.leader());
        struct.set(LEADER_URL_KEY_NAME, assignment.leaderUrl());
        struct.set(CONFIG_VERSION_KEY_NAME, assignment.version());
        Struct taskAssignment = new Struct(DATALINK_ASSIGNMENT_V0);
        taskAssignment.set(TASKS_KEY_NAME, assignment.tasks().toArray());
        struct.set(ASSIGNMENT_KEY_NAME, taskAssignment);

        ByteBuffer buffer = ByteBuffer.allocate(DATALINK_PROTOCOL_HEADER_V0.sizeOf() + ASSIGNMENT_V0.sizeOf(struct));
        DATALINK_PROTOCOL_HEADER_V0.writeTo(buffer);
        ASSIGNMENT_V0.write(buffer, struct);
        buffer.flip();
        return buffer;
    }

    public static Assignment deserializeAssignment(ByteBuffer buffer) {
        Struct header = DATALINK_PROTOCOL_HEADER_SCHEMA.read(buffer);
        Short version = header.getShort(VERSION_KEY_NAME);
        checkVersionCompatibility(version);
        Struct struct = ASSIGNMENT_V0.read(buffer);
        short error = struct.getShort(ERROR_KEY_NAME);
        String leader = struct.getString(LEADER_KEY_NAME);
        String leaderUrl = struct.getString(LEADER_URL_KEY_NAME);
        long offset = struct.getLong(CONFIG_VERSION_KEY_NAME);

        List<String> taskIds = new ArrayList<String>();
        Struct assignment = (Struct) struct.get(ASSIGNMENT_KEY_NAME);
        for (Object taskIdObj : assignment.getArray(TASKS_KEY_NAME)) {
            taskIds.add(taskIdObj.toString());
        }
        return new Assignment(error, leader, leaderUrl, offset, taskIds);
    }

    public static class WorkerState {
        private final String url;
        private final long version;

        public WorkerState(String url, long version) {
            this.url = url;
            this.version = version;
        }

        public String url() {
            return url;
        }

        public long version() {
            return version;
        }

        @Override
        public String toString() {
            return "WorkerState{" +
                    "url='" + url + '\'' +
                    ", version=" + version +
                    '}';
        }
    }

    public static class Assignment {
        public static final short NO_ERROR = 0;
        // Configuration offsets mismatched in a way that the leader could not resolve. Workers should read to the end
        // of the config rdbms and try to re-join
        public static final short CONFIG_MISMATCH = 1;

        private final short error;
        private final String leader;
        private final String leaderUrl;
        private final long version;
        private final List<String> taskIds;

        /**
         * Create an assignment indicating responsibility for the given job instances and task Ids.
         *
         * @param taskIds list of task IDs that the worker should instantiate and run
         */
        public Assignment(short error, String leader, String leaderUrl, long configVersion, List<String> taskIds) {
            this.error = error;
            this.leader = leader;
            this.leaderUrl = leaderUrl;
            this.version = configVersion;
            this.taskIds = taskIds;
        }

        public short error() {
            return error;
        }

        public String leader() {
            return leader;
        }

        public String leaderUrl() {
            return leaderUrl;
        }

        public boolean failed() {
            return error != NO_ERROR;
        }

        public long version() {
            return version;
        }

        public List<String> tasks() {
            return taskIds;
        }

        @Override
        public String toString() {
            return "Assignment{" +
                    "error=" + error +
                    ", leader='" + leader + '\'' +
                    ", leaderUrl='" + leaderUrl + '\'' +
                    ", version=" + version +
                    ", taskIds=" + taskIds +
                    '}';
        }
    }

    private static void checkVersionCompatibility(short version) {
        // check for invalid versions
        if (version < DATALINK_PROTOCOL_V0)
            throw new SchemaException("Unsupported subscription version: " + version);

        // otherwise, assume versions can be parsed as V0
    }

}
