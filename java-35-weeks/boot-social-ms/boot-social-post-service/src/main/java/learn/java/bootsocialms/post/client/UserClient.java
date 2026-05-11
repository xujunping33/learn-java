package learn.java.bootsocialms.post.client;

import learn.java.bootsocialms.post.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "boot-social-user-service")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserResponse getById(@PathVariable("id") Long id);
}

