package com.yourname.redditclone.dto.response;

import java.util.List;

/** Response wrapper for paged lists. */
public class PagedResponse<T> extends ApiResponse<List<T>> {
  private PageInfo pageInfo;

  public PagedResponse() {
    super();
  }

  public PagedResponse(List<T> data, PageInfo pageInfo) {
    super(true, data, null, null);
    this.pageInfo = pageInfo;
  }

  public PageInfo getPageInfo() {
    return pageInfo;
  }

  public void setPageInfo(PageInfo pageInfo) {
    this.pageInfo = pageInfo;
  }

  public static class PageInfo {
    private long totalElements;
    private int page;
    private int size;
    private int totalPages;

    public PageInfo() {}

    public long getTotalElements() {
      return totalElements;
    }

    public void setTotalElements(long totalElements) {
      this.totalElements = totalElements;
    }

    public int getPage() {
      return page;
    }

    public void setPage(int page) {
      this.page = page;
    }

    public int getSize() {
      return size;
    }

    public void setSize(int size) {
      this.size = size;
    }

    public int getTotalPages() {
      return totalPages;
    }

    public void setTotalPages(int totalPages) {
      this.totalPages = totalPages;
    }
  }
}
