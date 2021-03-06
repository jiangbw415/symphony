/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.repository;

import java.util.ArrayList;
import java.util.List;
import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Link;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Tag-User-Link relation repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Sep 11, 2016
 * @since 1.6.0
 */
@Repository
public class TagUserLinkRepository extends AbstractRepository {

    /**
     * Counts link (distinct(linkId)) with the specified tag id.
     *
     * @param tagId the specified tag id
     * @return count
     * @throws RepositoryException repository exception
     */
    public int countTagLink(final String tagId) throws RepositoryException {
        final List<JSONObject> result = select("SELECT count(DISTINCT(linkId)) AS `ret` FROM `" + getName()
                + "` WHERE `tagId` = ?", tagId);

        return result.get(0).optInt("ret");
    }

    /**
     * Updates link score with the specified tag id, link id and score.
     *
     * @param tagId the specified tag id
     * @param linkId the specified link id
     * @param score the specified score
     * @throws RepositoryException repository exception
     */
    public void updateTagLinkScore(final String tagId, final String linkId, final double score)
            throws RepositoryException {
        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Tag.TAG_T_ID, FilterOperator.EQUAL, tagId),
                        new PropertyFilter(Link.LINK_T_ID, FilterOperator.EQUAL, linkId)
                )).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray relations = result.optJSONArray(Keys.RESULTS);
        for (int i = 0; i < relations.length(); i++) {
            final JSONObject rel = relations.optJSONObject(i);
            rel.put(Link.LINK_SCORE, score);

            update(rel.optString(Keys.OBJECT_ID), rel);
        }
    }

    /**
     * Removes tag-user-link relations by the specified tag id, user id and link id.
     *
     * @param tagId the specified tag id
     * @param userId the specified user id
     * @param linkId the specified link id
     * @throws RepositoryException repository exception
     */
    public void removeByTagIdUserIdAndLinkId(final String tagId, final String userId, final String linkId)
            throws RepositoryException {
        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Tag.TAG_T_ID, FilterOperator.EQUAL, tagId),
                        new PropertyFilter(UserExt.USER_T_ID, FilterOperator.EQUAL, userId),
                        new PropertyFilter(Link.LINK_T_ID, FilterOperator.EQUAL, linkId)
                )).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray relations = result.optJSONArray(Keys.RESULTS);
        for (int i = 0; i < relations.length(); i++) {
            final JSONObject rel = relations.optJSONObject(i);

            remove(rel.optString(Keys.OBJECT_ID));
        }
    }

    /**
     * Gets tag-link relations by the specified tag id and fetch size (distinct(linkId), order by score).
     *
     * @param tagId the specified tag id
     * @param fetchSize the specified fetch size
     * @return a list of link id
     * @throws RepositoryException repository exception
     */
    public List<String> getByTagId(final String tagId, final int fetchSize) throws RepositoryException {
        final List<JSONObject> results = select("SELECT DISTINCT(`linkId`), `linkScore` FROM `" + getName()
                + "` WHERE `tagId` = ? ORDER BY `linkScore` DESC LIMIT ?", tagId, fetchSize);

        final List<String> ret = new ArrayList<>();
        for (final JSONObject result : results) {
            ret.add(result.optString(Link.LINK_T_ID));
        }

        return ret;
    }

    /**
     * Gets tag-link relations by the specified tag id, user id and fetch size (distinct(linkId), order by score).
     *
     * @param tagId the specified tag id
     * @param userId the specified user id
     * @param fetchSize the specified fetch size
     * @return a list of link id
     * @throws RepositoryException repository exception
     */
    public List<String> getByTagIdAndUserId(final String tagId, final String userId, final int fetchSize)
            throws RepositoryException {
        final List<JSONObject> results = select("SELECT `linkId` FROM `" + getName()
                + "` WHERE `tagId` = ? AND `userId` = ? ORDER BY `linkScore` DESC LIMIT ?", tagId, userId, fetchSize);

        final List<String> ret = new ArrayList<>();
        for (final JSONObject result : results) {
            ret.add(result.optString(Link.LINK_T_ID));
        }

        return ret;
    }

    /**
     * Public constructor.
     */
    public TagUserLinkRepository() {
        super(Tag.TAG + "_" + User.USER + "_" + Link.LINK);
    }
}
