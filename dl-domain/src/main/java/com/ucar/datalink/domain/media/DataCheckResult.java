package com.ucar.datalink.domain.media;

public class DataCheckResult {
    private Long toDsCount;
    private Long fromDsCount;
    private Long toMaxId;
    private Long fromMaxId;
    private Long toMinId;
    private Long fromMinId;

    public Long getToDsCount() {
        return toDsCount;
    }

    public void setToDsCount(Long toDsCount) {
        this.toDsCount = toDsCount;
    }

    public Long getFromDsCount() {
        return fromDsCount;
    }

    public void setFromDsCount(Long fromDsCount) {
        this.fromDsCount = fromDsCount;
    }

    public Long getToMaxId() {
        return toMaxId;
    }

    public void setToMaxId(Long toMaxId) {
        this.toMaxId = toMaxId;
    }

    public Long getFromMaxId() {
        return fromMaxId;
    }

    public void setFromMaxId(Long fromMaxId) {
        this.fromMaxId = fromMaxId;
    }

    public Long getToMinId() {
        return toMinId;
    }

    public void setToMinId(Long toMinId) {
        this.toMinId = toMinId;
    }

    public Long getFromMinId() {
        return fromMinId;
    }

    public void setFromMinId(Long fromMinId) {
        this.fromMinId = fromMinId;
    }
}
