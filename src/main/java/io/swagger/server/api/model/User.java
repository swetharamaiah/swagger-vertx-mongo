package io.swagger.server.api.model;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.vertx.core.json.JsonObject;

@JsonInclude(JsonInclude.Include.NON_NULL) 
public class User   {

  private static final AtomicInteger COUNTER = new AtomicInteger();

  private String userId = null;

  public JsonObject toJson() {
    JsonObject json = new JsonObject()
            .put("name", name)
            .put("userType", userType.toString())
            .put("userName", userName)
            .put("email", email);
    json.put("_id", userId);
    return json;
  }


  public enum UserTypeEnum {
    SELLER("Seller"),
    BUYER("Buyer");

    private String value;

    UserTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return value;
    }
  }

  private UserTypeEnum userType = null;
  private String userName = null;
  private String name = null;
  private String email = null;

  public User () {
//    this.userId = COUNTER.getAndIncrement();
  }

  public User (String userId, UserTypeEnum userType, String userName, String name, String email) {
    this.userId = userId;
    this.userType = userType;
    this.userName = userName;
    this.name = name;
    this.email = email;
  }

    
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }
  public User setUserId(String id) {
    this.userId = id;
    return this;
  }

    
  @JsonProperty("userType")
  public UserTypeEnum getUserType() {
    return userType;
  }
  public void setUserType(UserTypeEnum userType) {
    this.userType = userType;
  }

    
  @JsonProperty("userName")
  public String getUserName() {
    return userName;
  }
  public void setUserName(String userName) {
    this.userName = userName;
  }

    
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

    
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(userId, user.userId) &&
        Objects.equals(userType, user.userType) &&
        Objects.equals(userName, user.userName) &&
        Objects.equals(name, user.name) &&
        Objects.equals(email, user.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, userType, userName, name, email);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class User {\n");
    
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    userType: ").append(toIndentedString(userType)).append("\n");
    sb.append("    userName: ").append(toIndentedString(userName)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  public User generateUserId() {
    String uniqueId = UUID.randomUUID().toString().substring(0, 5);
    String userId = this.getUserName()+"_"+uniqueId;
    System.out.println(userId);
    this.setUserId(userId);
    return this;
  }
  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
