package com.ucar.datalink.manager.core.web.util;

import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2017/1/22.
 */
public class Page<T> {

    private int draw; // 第几次请求

    private int start; // 开始的数据行数

    private int length; // 每页的数据数

    private int recordsTotal; // 即没有过滤的记录数（数据库里总共记录数）

    private int recordsFiltered; // 过滤后的记录数（如果有接收到前台的过滤条件，则返回的是过滤后的记录数）

    private List<T> aaData; // 表中中需要显示的数据

    private int pageSize = 10;

    private int pageNum; // 当前页数

    private int pages; // 总页数

    private int size; // 当前页的数量<=pageSize

    public Page(List<T> aaData){
        this.aaData = aaData;
    }

    public Page(int start, int length, List<T> aaData){
        this.start = start;
        this.length = length;
        this.aaData = aaData;
    }

    public Page(Map<String, String> map) {
        String start = map.get("start");
        String length = map.get("length");
        String draw = map.get("draw");

        this.setStart(Integer.parseInt(start));
        this.setLength(Integer.parseInt(length));
        this.setDraw(Integer.parseInt(draw));
        //计算当前页码
        this.pageNum = (Integer.parseInt(start) / Integer.parseInt(length)) + 1;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<T> getAaData() {
        return aaData;
    }

    public void setAaData(List<T> aaData) {
        this.aaData = aaData;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

