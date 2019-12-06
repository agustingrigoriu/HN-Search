SELECT
  p.id,
  p.title,
  p.url,
  p.text,
  p.by,
  p.score,
  p.time,
  p.timestamp,
  ARRAY(
  SELECT
    AS STRUCT c0.text
  FROM
    `bigquery-public-data.hacker_news.full` c0
  WHERE
    c0.parent = p.id)
FROM
  `bigquery-public-data.hacker_news.full` p
WHERE
  p.type = 'story'
  AND timestamp > TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 26280 HOUR)
ORDER BY
  timestamp ASC;