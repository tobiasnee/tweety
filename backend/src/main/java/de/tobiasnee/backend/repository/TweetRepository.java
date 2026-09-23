package de.tobiasnee.backend.repository;

import de.tobiasnee.backend.entity.TweetEntity;
import de.tobiasnee.backend.repository.projection.TweetListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TweetRepository extends JpaRepository<TweetEntity, Long> {

    @Query(value = """
            select new de.tobiasnee.backend.repository.projection.TweetListItem(
                t.id, t.text, t.createdAt, a.id, a.username, a.displayName)
            from TweetEntity t
            join t.author a
            order by t.createdAt desc
            """,
            countQuery = "select count(t) from TweetEntity t")
    Page<TweetListItem> findTimeline(Pageable pageable);

    @Query(value = """
            select new de.tobiasnee.backend.repository.projection.TweetListItem(
                t.id, t.text, t.createdAt, a.id, a.username, a.displayName)
            from TweetEntity t
            join t.author a
            where a.id = :authorId
            order by t.createdAt desc
            """,
            countQuery = "select count(t) from TweetEntity t where t.author.id = :authorId")
    Page<TweetListItem> findTimelineByAuthor(@Param("authorId") Long authorId, Pageable pageable);
}