package learn.java.mybatis.model;

/**
 * 分页请求：offset = (page - 1) * pageSize，供 Mapper {@code LIMIT} 使用。
 */
public class PageRequest {
    private final int page;
    private final int pageSize;

    public PageRequest(int page, int pageSize) {
        this.page = page < 1 ? 1 : page;
        this.pageSize = pageSize < 1 ? 10 : Math.min(pageSize, 500);
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    /** MySQL：{@code LIMIT offset, pageSize} 中的 offset */
    public int getOffset() {
        return (page - 1) * pageSize;
    }
}
