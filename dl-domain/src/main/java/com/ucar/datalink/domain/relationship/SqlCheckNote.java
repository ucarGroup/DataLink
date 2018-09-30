package com.ucar.datalink.domain.relationship;

import com.ucar.datalink.domain.media.MediaSourceType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lubiao on 2017/7/15.
 */
public class SqlCheckNote implements Comparable<SqlCheckNote> {

    private RoleType roleType;
    private NoteLevel noteLevel;
    private String desc;

    public SqlCheckNote() {

    }

    public SqlCheckNote(String desc, RoleType roleType, NoteLevel noteLevel) {
        this.desc = desc;
        this.roleType = roleType;
        this.noteLevel = noteLevel;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public NoteLevel getNoteLevel() {
        return noteLevel;
    }

    public void setNoteLevel(NoteLevel noteLevel) {
        this.noteLevel = noteLevel;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SqlCheckNote that = (SqlCheckNote) o;

        if (roleType != that.roleType) return false;
        if (noteLevel != that.noteLevel) return false;
        return desc.equals(that.desc);

    }

    @Override
    public int hashCode() {
        int result = roleType.hashCode();
        result = 31 * result + noteLevel.hashCode();
        result = 31 * result + desc.hashCode();
        return result;
    }

    @Override
    public int compareTo(SqlCheckNote o) {
        if (noteLevel.compareTo(o.getNoteLevel()) == 0) {
            if (roleType.compareTo(o.getRoleType()) == 0) {
                return desc.compareTo(o.getDesc());
            } else {
                return roleType.compareTo(o.getRoleType());
            }
        } else {
            return noteLevel.compareTo(o.getNoteLevel());
        }
    }

    public static enum RoleType {
        ALL, //所有角色
        DBA, //数据库管理员
        ESA, //ES管理员
        DLA, //Datalink管理员
        SPARKA,//SPARK管理员
        HBASEA;//HBASE管理员

        private static Map<MediaSourceType, RoleType> typeMappings = new HashMap<>();

        static {
            typeMappings.put(MediaSourceType.ELASTICSEARCH, ESA);
            typeMappings.put(MediaSourceType.HDFS, SPARKA);
            typeMappings.put(MediaSourceType.MYSQL, DBA);
            typeMappings.put(MediaSourceType.SQLSERVER, DBA);
            typeMappings.put(MediaSourceType.POSTGRESQL, DBA);
            typeMappings.put(MediaSourceType.ORACLE, DBA);
            typeMappings.put(MediaSourceType.SDDL, DBA);
            typeMappings.put(MediaSourceType.HBASE, HBASEA);
        }

        public static RoleType getRoleTypeByMediaSourceType(MediaSourceType mediaSourceType) {
            return typeMappings.get(mediaSourceType);
        }
    }

    public static enum NoteLevel {
        ERROR, WARN, INFO
    }
}


