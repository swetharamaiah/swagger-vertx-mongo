package io.swagger.server.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.vertx.core.json.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL) 
public class Project   {
  
  private String id = null;
  private String name = null;
  private String description = null;

  public Project(String projectId, Double minBidAmount) {
    this.id = projectId;
    this.lowestBidAmount = minBidAmount;
  }

  public void addBid(Bid newBid) {
    bids.add(newBid);
  }

  public enum StatusEnum {
    OPEN("Open"),
    CLOSED("Closed"),
    INPROGRESS("InProgress");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return value;
    }
  }

  private StatusEnum status = null;
  private Date biddingExpiration = null;
  private Double maxBudget = null;
  List<Bid> bids = new ArrayList<>();
  private Double lowestBidAmount = null;

  public Project () {

  }

  public Project(JsonObject entries) {
    this.id = entries.getString("_id");
    this.name = entries.getString("name");
    this.description = entries.getString("description");
    this.status = entries.getString("status") == null ? StatusEnum.OPEN : StatusEnum.valueOf(entries.getString("status"));
    String str = entries.getString("biddingExpiration");
    SimpleDateFormat df = new SimpleDateFormat("E MMM dd hh:mm:ss Z yyyy");
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
    try {
      this.biddingExpiration = df.parse(str);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    this.maxBudget = Double.valueOf(entries.getString("maxBudget"));
    List<Bid> bidList = entries.getJsonArray("bids") != null && !entries.getJsonArray("bids").isEmpty() ? entries.getJsonArray("bids").getList() : new ArrayList<>();
    this.bids.addAll(bidList);
  }

  public Project (String id, String name, String description, StatusEnum status, Date biddingExpiration, Double maxBudget) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.status = status;
    this.biddingExpiration = biddingExpiration;
    this.maxBudget = maxBudget;
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public Project setId(String id) {
    this.id = id;
    return this;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

    
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

    
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

    
  @JsonProperty("biddingExpiration")
  public Date getBiddingExpiration() {
    return biddingExpiration;
  }
  public void setBiddingExpiration(Date biddingExpiration) {
    this.biddingExpiration = biddingExpiration;
  }

    
  @JsonProperty("maxBudget")
  public Double getMaxBudget() {
    return maxBudget;
  }
  public void setMaxBudget(Double maxBudget) {
    this.maxBudget = maxBudget;
  }

  public List<Bid> getBids() {
    return bids;
  }

  public void setBids(List<Bid> bids) {
    this.bids = bids;
  }

  public Double getLowestBidAmount() {
    return lowestBidAmount;
  }

  public void setLowestBidAmount(Double lowestBidAmount) {
    this.lowestBidAmount = lowestBidAmount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Project project = (Project) o;
    return Objects.equals(id, project.id) &&
        Objects.equals(name, project.name) &&
        Objects.equals(description, project.description) &&
        Objects.equals(status, project.status) &&
        Objects.equals(biddingExpiration, project.biddingExpiration) &&
        Objects.equals(maxBudget, project.maxBudget);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, status, biddingExpiration, maxBudget);
  }

  @Override
  public String toString() {
    return "class Project {\n" + "    id: " + toIndentedString(id) + "\n" + "    name: " +
            toIndentedString(name) + "\n" + "    description: " + toIndentedString(description) + "\n" +
            "    status: " + toIndentedString(status) + "\n" + "    biddingExpiration: " +
            toIndentedString(biddingExpiration) + "\n" + "    maxBudget: " + toIndentedString(maxBudget) + "\n" + "}";
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

  public JsonObject toJson() {
    JsonObject json = new JsonObject()
            .put("name", name)
            .put("description", description)
            .put("biddingExpiration", biddingExpiration.toString())
            .put("maxBudget", maxBudget.toString())
            .put("status", status)
            .put("bids", bids);
    json.put("_id", id);
    return json;
  }

}
