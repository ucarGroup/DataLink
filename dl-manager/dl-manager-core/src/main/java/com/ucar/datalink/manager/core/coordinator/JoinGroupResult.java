package com.ucar.datalink.manager.core.coordinator;

import java.util.Map;

/**
 * Created by lubiao on 2016/11/30.
 */
public class JoinGroupResult {
    private Map<String, byte[]> members;
    private String memberId;
    private Integer generationId;
    private String subProtocol;
    private String leaderId;
    private Short errorCode;

    public JoinGroupResult(Map<String, byte[]> members, String memberId, Integer generationId, String subProtocol, String leaderId, Short errorCode) {
        this.members = members;
        this.memberId = memberId;
        this.generationId = generationId;
        this.subProtocol = subProtocol;
        this.leaderId = leaderId;
        this.errorCode = errorCode;
    }

    public Map<String, byte[]> getMembers() {
        return members;
    }

    public void setMembers(Map<String, byte[]> members) {
        this.members = members;
    }

    public String getMemberId() {
        return memberId;
    }

    public Integer getGenerationId() {
        return generationId;
    }

    public void setGenerationId(Integer generationId) {
        this.generationId = generationId;
    }

    public String getSubProtocol() {
        return subProtocol;
    }

    public void setSubProtocol(String subProtocol) {
        this.subProtocol = subProtocol;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public Short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Short errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinGroupResult that = (JoinGroupResult) o;

        if (members != null ? !members.equals(that.members) : that.members != null) return false;
        if (memberId != null ? !memberId.equals(that.memberId) : that.memberId != null) return false;
        if (generationId != null ? !generationId.equals(that.generationId) : that.generationId != null) return false;
        if (subProtocol != null ? !subProtocol.equals(that.subProtocol) : that.subProtocol != null) return false;
        if (leaderId != null ? !leaderId.equals(that.leaderId) : that.leaderId != null) return false;
        return !(errorCode != null ? !errorCode.equals(that.errorCode) : that.errorCode != null);

    }

    @Override
    public int hashCode() {
        int result = members != null ? members.hashCode() : 0;
        result = 31 * result + (memberId != null ? memberId.hashCode() : 0);
        result = 31 * result + (generationId != null ? generationId.hashCode() : 0);
        result = 31 * result + (subProtocol != null ? subProtocol.hashCode() : 0);
        result = 31 * result + (leaderId != null ? leaderId.hashCode() : 0);
        result = 31 * result + (errorCode != null ? errorCode.hashCode() : 0);
        return result;
    }
}
