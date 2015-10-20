package dao

import model.KontrollettiToModelParser
import model.Commit
import play.api.libs.json.Json
import slick.jdbc.{ StaticQuery => Q, GetResult }

object Queries {
  import dao.KontrollettiPostgresDriver.api._
  import utility.FutureUtil._

  def selectRangeOfCommits(host: String, project: String, repository: String, since: String, until: String, pageNumber: Int, maxPerPage: Int) = {
    implicit val getCommits = GetResult[Commit](r => utility.Transformer.deserialize(Json.parse(r.nextString()))(KontrollettiToModelParser.commitReader))
    val limit = maxPerPage
    val offset = if (pageNumber > 1) ((pageNumber - 1) * maxPerPage) else 0

    sql"""
        WITH RECURSIVE PARENTS(parent_id, id, date) AS (
          WITH FIRST_COMMIT(parent_id, id, date) AS (
          SELECT ( CASE WHEN array_upper(parent_ids, 1) is null then null
             ELSE unnest(parent_ids) END ) as parent_id,
              id,
              date
             FROM kont_data."COMMITS" C, kont_data."REPOSITORIES" R
            WHERE  id = $since
              AND R.host = $host
              AND R.project = $project
              AND R.repository = $repository
             AND C.repository_url = R.URL
          ),
          LAST_COMMIT(parent_id, id, date) AS (
            SELECT ( CASE WHEN array_upper(parent_ids, 1) is null then null
             ELSE unnest(parent_ids) END ) as parent_id,
                id,
                date
              FROM kont_data."COMMITS" C, kont_data."REPOSITORIES" R
                   WHERE id = $until
                   AND R.host =  $host
                   AND R.project = $project
                   AND R.repository = $repository
                   AND C.repository_url = R.URL
          ),
          ALL_COMMITS(parent_id, id, date) AS (
            SELECT ( CASE WHEN array_upper(parent_ids, 1) is null then null
             ELSE unnest(parent_ids) END ) as parent_id,
                C.id,
                C.date
              FROM kont_data."COMMITS" C, kont_data."REPOSITORIES" R, LAST_COMMIT L, FIRST_COMMIT F
                   WHERE  R.host = $host
                     AND R.project = $project
                     AND R.repository = $repository
                     AND C.repository_url = R.URL
                     AND C.date >= L.date
                     AND C.date <= F.date
          )
          SELECT F.parent_id, F.id, F.date
            FROM FIRST_COMMIT AS F
          UNION
          SELECT L.parent_id, L.id, L.date
            FROM LAST_COMMIT L
          UNION ALL
          SELECT  A.Parent_id, A.id, A.date
             FROM ALL_COMMITS AS A, PARENTS AS P, LAST_COMMIT AS LAST
            WHERE A.id = P.parent_id
              AND A.date >= LAST.date
              AND A.date <= P.date
        )
        SELECT C.json_value
          FROM PARENTS P, kont_data."COMMITS" C
               WHERE C.id = P.id
            GROUP BY C.ID
            ORDER BY C.date DESC
            limit $limit offset $offset""".as[Commit]
  }
}
