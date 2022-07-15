package cooba.IndustryPerformance.database.mapper;

import cooba.IndustryPerformance.database.entity.Rss.Rss;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RssMapper {
    void insertRss(Rss rss);

    List<Rss> findByPublishedDateGreaterThan(@Param("publishedDate") LocalDateTime localDateTime);
}

