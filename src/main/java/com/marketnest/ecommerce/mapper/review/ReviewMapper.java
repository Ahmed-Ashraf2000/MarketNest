package com.marketnest.ecommerce.mapper.review;

import com.marketnest.ecommerce.dto.review.CreateReviewRequest;
import com.marketnest.ecommerce.dto.review.ReviewResponse;
import com.marketnest.ecommerce.dto.review.UpdateReviewRequest;
import com.marketnest.ecommerce.model.Review;
import com.marketnest.ecommerce.util.HtmlEscapeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ReviewMapper {

    @Autowired
    protected HtmlEscapeUtil htmlEscapeUtil;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "title", expression = "java(htmlEscapeUtil.escapeHtml(request.getTitle()))")
    @Mapping(target = "comment",
            expression = "java(htmlEscapeUtil.escapeHtml(request.getComment()))")
    @Mapping(target = "verifiedPurchase", constant = "false")
    @Mapping(target = "helpfulCount", constant = "0")
    @Mapping(target = "isApproved", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Review toEntity(CreateReviewRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "title", expression = "java(htmlEscapeUtil.escapeHtml(request.getTitle()))")
    @Mapping(target = "comment",
            expression = "java(htmlEscapeUtil.escapeHtml(request.getComment()))")
    @Mapping(target = "verifiedPurchase", ignore = true)
    @Mapping(target = "helpfulCount", ignore = true)
    @Mapping(target = "isApproved", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntityFromDto(UpdateReviewRequest request,
                                             @MappingTarget Review review);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userName", expression = "java(getUserName(review))")
    public abstract ReviewResponse toResponse(Review review);

    protected String getUserName(Review review) {
        if (review.getUser() == null) {
            return null;
        }
        return review.getUser().getFirstName() + " " + review.getUser().getLastName();
    }
}