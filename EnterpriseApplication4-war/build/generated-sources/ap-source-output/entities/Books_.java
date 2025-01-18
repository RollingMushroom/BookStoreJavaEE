package entities;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.12.v20230209-rNA", date="2025-01-19T06:06:33")
@StaticMetamodel(Books.class)
public class Books_ { 

    public static volatile SingularAttribute<Books, String> author;
    public static volatile SingularAttribute<Books, BigDecimal> price;
    public static volatile SingularAttribute<Books, String> title;
    public static volatile SingularAttribute<Books, String> category;
    public static volatile SingularAttribute<Books, Integer> bookId;

}