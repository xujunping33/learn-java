public class Book {
    public int id;
    public String title;
    public String author;
    public double price;

    @Override
    public String toString() {
        return "Book{id=" + id
                + ", title='" + title + '\''
                + ", author='" + author + '\''
                + ", price=" + price
                + "}";
    }
}

