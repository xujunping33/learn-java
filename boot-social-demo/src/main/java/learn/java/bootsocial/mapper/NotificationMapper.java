package learn.java.bootsocial.mapper;

import learn.java.bootsocial.model.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper {
    int insertIgnore(Notification notification);
}

