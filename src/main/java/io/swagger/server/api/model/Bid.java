package io.swagger.server.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;

@JsonInclude(JsonInclude.Include.NON_NULL) 
public class Bid {
  
  private String projectId = null;
  private Double bidAmount = null;
  private Integer sellerId = null;
  private Integer buyerId = null;

  public Bid () {

  }

  public Bid (Double bidAmount, Integer sellerId, Integer buyerId) {
    this.bidAmount = bidAmount;
    this.sellerId = sellerId;
    this.buyerId = buyerId;
  }

  public Bid(Integer buyerId, Double biddingAmount) {
    this.buyerId = buyerId;
    this.bidAmount = biddingAmount;
  }


  @JsonProperty("projectId")
  public String getProjectId() {
    return projectId;
  }

    
  @JsonProperty("bidAmount")
  public Double getBidAmount() {
    return bidAmount;
  }
  public void setBidAmount(Double bidAmount) {
    this.bidAmount = bidAmount;
  }

    
  @JsonProperty("sellerId")
  public Integer getSellerId() {
    return sellerId;
  }
  public void setSellerId(Integer sellerId) {
    this.sellerId = sellerId;
  }

    
  @JsonProperty("buyerId")
  public Integer getBuyerId() {
    return buyerId;
  }
  public void setBuyerId(Integer buyerId) {
    this.buyerId = buyerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Bid bid = (Bid) o;
    return Objects.equals(projectId, bid.projectId) &&
        Objects.equals(bidAmount, bid.bidAmount) &&
        Objects.equals(sellerId, bid.sellerId) &&
        Objects.equals(buyerId, bid.buyerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectId, bidAmount, sellerId, buyerId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Bid {\n");

    sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
    sb.append("    bidAmount: ").append(toIndentedString(bidAmount)).append("\n");
    sb.append("    sellerId: ").append(toIndentedString(sellerId)).append("\n");
    sb.append("    buyerId: ").append(toIndentedString(buyerId)).append("\n");
    sb.append("}");
    return sb.toString();
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
    return new JsonObject()
            .put("bidAmount", bidAmount.toString())
            .put("sellerId", sellerId.toString())
            .put("buyerId", buyerId.toString());
  }
}
