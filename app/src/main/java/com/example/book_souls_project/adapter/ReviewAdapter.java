package com.example.book_souls_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.book_souls_project.R;
import com.example.book_souls_project.api.types.review.Review;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    
    private List<Review> reviews = new ArrayList<>();

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addReviews(List<Review> newReviews) {
        if (newReviews != null) {
            int startPosition = this.reviews.size();
            this.reviews.addAll(newReviews);
            notifyItemRangeInserted(startPosition, newReviews.size());
        }
    }

    public void addReview(Review review) {
        if (review != null) {
            reviews.add(0, review); // Add to top
            notifyItemInserted(0);
        }
    }

    public void clearReviews() {
        reviews.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageUserAvatar;
        private final TextView textUserName;
        private final TextView textReviewDate;
        private final TextView textReviewContent;
        private final TextView textRatingValue;
        
        // Rating stars
        private final ImageView ratingStar1;
        private final ImageView ratingStar2;
        private final ImageView ratingStar3;
        private final ImageView ratingStar4;
        private final ImageView ratingStar5;
        
        private final List<ImageView> ratingStars;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageUserAvatar = itemView.findViewById(R.id.imageUserAvatar);
            textUserName = itemView.findViewById(R.id.textUserName);
            textReviewDate = itemView.findViewById(R.id.textReviewDate);
            textReviewContent = itemView.findViewById(R.id.textReviewContent);
            textRatingValue = itemView.findViewById(R.id.textRatingValue);
            
            // Initialize rating stars
            ratingStar1 = itemView.findViewById(R.id.ratingStar1);
            ratingStar2 = itemView.findViewById(R.id.ratingStar2);
            ratingStar3 = itemView.findViewById(R.id.ratingStar3);
            ratingStar4 = itemView.findViewById(R.id.ratingStar4);
            ratingStar5 = itemView.findViewById(R.id.ratingStar5);
            
            ratingStars = Arrays.asList(ratingStar1, ratingStar2, ratingStar3, ratingStar4, ratingStar5);
        }

        public void bind(Review review) {
            // Set user information from the nested user object
            if (review.getUser() != null) {
                textUserName.setText(review.getUser().getFullName());
                
                // Load user avatar
                if (review.getUser().getAvatar() != null && !review.getUser().getAvatar().isEmpty()) {
                    Glide.with(itemView.getContext())
                        .load(review.getUser().getAvatar())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(imageUserAvatar);
                } else {
                    imageUserAvatar.setImageResource(R.drawable.ic_person);
                }
            } else {
                textUserName.setText("Anonymous User");
                imageUserAvatar.setImageResource(R.drawable.ic_person);
            }
            
            // Set review information
            textReviewDate.setText(review.getFormattedDate());
            textReviewContent.setText(review.getComment());
            textRatingValue.setText(String.valueOf(review.getRating()));
            
            // Set rating stars
            updateRatingStars((float) review.getRating());
        }
        
        private void updateRatingStars(float rating) {
            int fullStars = (int) rating;
            boolean hasHalfStar = rating - fullStars >= 0.5f;
            
            for (int i = 0; i < ratingStars.size(); i++) {
                ImageView star = ratingStars.get(i);
                if (i < fullStars) {
                    star.setImageResource(R.drawable.ic_star);
                } else if (i == fullStars && hasHalfStar) {
                    star.setImageResource(R.drawable.ic_star_half);
                } else {
                    star.setImageResource(R.drawable.ic_star_border);
                }
            }
        }
    }
}
