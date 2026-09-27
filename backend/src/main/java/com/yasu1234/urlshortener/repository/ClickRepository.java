package com.yasu1234.urlshortener.repository;

import com.yasu1234.urlshortener.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickRepository extends JpaRepository<Click, Long> {

    // 日別集計はSQLのGROUP BYでDBに任せる（アプリ側でclicksを全件取得してループ集計しない）
    @Query(
            value = """
                    SELECT CAST(clicked_at AS date) AS day, COUNT(*) AS count
                    FROM clicks
                    WHERE url_id = :urlId
                    GROUP BY CAST(clicked_at AS date)
                    ORDER BY day
                    """,
            nativeQuery = true)
    List<DailyClickCount> countDailyClicksByUrlId(@Param("urlId") Long urlId);
}
