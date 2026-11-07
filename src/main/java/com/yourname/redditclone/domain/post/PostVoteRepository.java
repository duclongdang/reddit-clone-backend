package com.yourname.redditclone.domain.post;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
    @Modifying
    @Query(value = "UPDATE posts SET score = score + :delta WHERE id = :postId", nativeQuery = true)
    int updateScore(@Param("postId") Long postId, @Param("delta") int delta);
}
