package com.canteen.repository;

import com.canteen.model.Canteen;
import com.canteen.model.Feedback;
import com.canteen.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByMenuItem(MenuItem menuItem);
    List<Feedback> findByCanteen(Canteen canteen);
    List<Feedback> findByMenuItemId(Long menuItemId);
    List<Feedback> findByCanteenId(Long canteenId);
}
