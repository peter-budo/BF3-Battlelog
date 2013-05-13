package com.ninetwozero.bf3droid.http;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ninetwozero.bf3droid.datatype.*;
import com.ninetwozero.bf3droid.jsonmodel.forum.ForumCategories;
import com.ninetwozero.bf3droid.misc.CacheHandler;
import com.ninetwozero.bf3droid.misc.Constants;
import com.ninetwozero.bf3droid.misc.SessionKeeper;
import com.ninetwozero.bf3droid.service.JsonProducer;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ForumClient extends DefaultClient {

    private long forumId;
    private long threadId;

    public static final String URL_LIST = Constants.URL_MAIN + "forum/";
    public static final String URL_LIST_LOCALIZED = Constants.URL_MAIN + "{LOCALE}/forum/";
    public static final String URL_FORUM = URL_LIST_LOCALIZED + "view/{FORUM_ID}/{PAGE}/";
    public static final String URL_THREAD = URL_LIST_LOCALIZED + "threadview/{THREAD_ID}/{PAGE}/";
    public static final String URL_POST = URL_LIST + "createpost/{THREAD_ID}/";
    public static final String URL_SIMILAR = URL_LIST + "dofindsimilar?title={title}&body={body}";
    public static final String URL_NEW = URL_LIST + "createthread/{FORUM_ID}/";
    public static final String URL_SEARCH = URL_LIST + "dosearch/?q={SEARCH_STRING}&snippet=text&fetch=threadId%2CforumId%2Ctitle%2Ctimestamp";
    public static final String URL_REPORT = Constants.URL_MAIN + "viewcontent/reportForumAbuse/{POST_ID}/";

    public static final String[] FIELD_NAMES_POST = new String[]{"body", "post-check-sum"};

    public static final String[] FIELD_NAMES_NEW = new String[]{"topic", "body", "post-check-sum"};

    public static final String[] FIELD_NAMES_REPORT = new String[]{"reason", "post-check-sum"};

    public void setForumId(long i) {
        forumId = i;
    }

    public void setThreadId(long i) {
        threadId = i;
    }

    public ForumClient() {
        mRequestHandler = new RequestHandler();
    }

    public static boolean reportPost(Context c, long pId, String r)
            throws WebsiteHandlerException {

        try {
            final String httpContent = new RequestHandler().post(
                    RequestHandler.generateUrl(URL_REPORT, pId),
                    RequestHandler.generatePostData(FIELD_NAMES_REPORT, r, ""),
                    RequestHandler.HEADER_AJAX
            );

            return (new JSONObject(httpContent).getJSONObject("data").getInt("success") == 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebsiteHandlerException("Could not report the post.");
        }
    }

    public List<ForumThreadData> getThreads(String locale, int page) throws WebsiteHandlerException {
        try {
            List<ForumThreadData> threads = new ArrayList<ForumThreadData>();

            final String httpContent = mRequestHandler.get(
                    RequestHandler.generateUrl(URL_FORUM, locale, forumId, page), RequestHandler.HEADER_AJAX
            );

            JSONObject contextObject = new JSONObject(httpContent).getJSONObject("context");
            JSONArray stickiesArray = contextObject.getJSONArray("stickies");
            JSONArray threadArray = contextObject.getJSONArray("threads");

            int numStickies = stickiesArray.length();
            for (int i = 0, max = numStickies; i < max; i++) {
                if (i == 0 && page == 1) {
                    threads.add(new ForumThreadData("Stickies"));
                }

                JSONObject currObject = stickiesArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject.getJSONObject("lastPoster");

                threads.add(
                        new ForumThreadData(
                                Long.parseLong(currObject.getString("id")),
                                forumId, currObject
                                .getLong("creationDate"), currObject
                                .getLong("lastPostDate"), currObject
                                .getInt("numberOfOfficialPosts"), currObject
                                .getInt("numberOfPosts"),
                                currObject.getString("title"),
                                new ProfileData.Builder(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username")
                                ).gravatarHash(ownerObject.getString("gravatarMd5")).build(),
                                new ProfileData.Builder(
                                        Long.parseLong(lastPosterObject.getString("userId")),
                                        lastPosterObject.getString("username")
                                ).gravatarHash(lastPosterObject.getString("gravatarMd5")).build(),
                                currObject.getBoolean("isSticky"),
                                currObject.getBoolean("isLocked")
                        )
                );
            }

            for (int i = numStickies, max = threadArray.length(); i < max; i++) {
                if (i == numStickies && page == 1) {
                    threads.add(new ForumThreadData("Threads"));
                }

                JSONObject currObject = threadArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject.getJSONObject("lastPoster");

                threads.add(
                        new ForumThreadData(
                                Long.parseLong(currObject.getString("id")),
                                forumId, currObject
                                .getLong("creationDate"), currObject
                                .getLong("lastPostDate"), currObject
                                .getInt("numberOfOfficialPosts"), currObject
                                .getInt("numberOfPosts"),
                                currObject.getString("title"),
                                new ProfileData.Builder(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username")
                                ).gravatarHash(ownerObject.getString("gravatarMd5")).build(),
                                new ProfileData.Builder(
                                        Long.parseLong(lastPosterObject.getString("userId")),
                                        lastPosterObject.getString("username")
                                ).gravatarHash(lastPosterObject.getString("gravatarMd5")).build(),
                                currObject.getBoolean("isSticky"),
                                currObject.getBoolean("isLocked")
                        )
                );
            }
            return threads;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebsiteHandlerException("No threads found.");
        }
    }

    public List<ForumPostData> getPosts(int page, String locale) throws WebsiteHandlerException {
        try {
            List<ForumPostData> posts = new ArrayList<ForumPostData>();
            final String httpContent = mRequestHandler.get(
                    RequestHandler.generateUrl(URL_THREAD, locale, threadId, page),
                    RequestHandler.HEADER_AJAX
            );

            JSONArray postArray = new JSONObject(httpContent).getJSONObject("context").getJSONArray("posts");

            for (int i = 0, max = postArray.length(); i < max; i++) {
                JSONObject currObject = postArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");

                posts.add(
                        new ForumPostData(
                                Long.parseLong(currObject.getString("id")), Long
                                .parseLong(currObject.getString("creationDate")), Long
                                .parseLong(currObject.getString("threadId")),
                                new ProfileData.Builder(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username")
                                ).gravatarHash(ownerObject.getString("gravatarMd5")).build(),
                                currObject.getString("formattedBody"),
                                currObject.getInt("abuseCount"),
                                currObject.getBoolean("isCensored"),
                                currObject.getBoolean("isOfficial")
                        )
                );
            }
            return posts;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebsiteHandlerException("No posts found for the thread.");
        }
    }

    public static List<ForumSearchResult> search(final Context c, final String keyword) throws WebsiteHandlerException {
        List<ForumSearchResult> threads = new ArrayList<ForumSearchResult>();
        try {
            String httpContent = new RequestHandler().get(
                    RequestHandler.generateUrl(URL_SEARCH, keyword),
                    RequestHandler.HEADER_AJAX
            );
            if (!"".equals(httpContent)) {
                JSONArray threadArray = new JSONObject(httpContent).getJSONObject("data").getJSONArray("results");
                int numResults = threadArray.length();

                if (numResults > 0) {
                    for (int i = 0; i < numResults; i++) {
                        JSONObject currentItem = threadArray.getJSONObject(i);

                        threads.add(
                                new ForumSearchResult(
                                        Long.parseLong(currentItem.getString("docid").substring(2)),
                                        currentItem.getLong("timestamp"),
                                        currentItem.getString("title"),
                                        new ProfileData.Builder(
                                                Long.parseLong(currentItem.getString("ownerId")),
                                                currentItem.getString("ownerUsername")).build(),
                                        currentItem.getBoolean("isSticky"),
                                        currentItem.getBoolean("isOfficial")
                                )
                        );
                    }
                }
            }
            return threads;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());
        }
    }

    public ForumCategories getForums(String locale) throws WebsiteHandlerException {
        try {
            List<ForumData> forums = new ArrayList<ForumData>();
            String title = "";

            final String httpContent = mRequestHandler.get(
                    RequestHandler.generateUrl(URL_LIST_LOCALIZED, locale),
                    RequestHandler.HEADER_AJAX
            );
            JsonObject json = JsonProducer.jsonObjectFromString(httpContent, "categories");
            Gson gson = new Gson();
            return gson.fromJson(json, ForumCategories.class);
            /*JSONArray categoryArray = new JSONObject(httpContent).getJSONObject("context").getJSONArray("categories");
            JSONArray forumArray = categoryArray.getJSONObject(0).optJSONArray("forums");

            title = categoryArray.getJSONObject(0).getString("title");

            for (int i = 0, max = forumArray.length(); i < max; i++) {
                JSONObject currObject = forumArray.getJSONObject(i);
                JSONObject lastThread = currObject.optJSONObject("lastThread");

                if (lastThread == null) {
                    forums.add(
                            new ForumData(
                                    Long.parseLong(currObject.getString("id")), Long
                                    .parseLong(currObject.getString("categoryId")), 0,
                                    0, 0, currObject.getLong("numberOfPosts"),
                                    currObject.getLong("numberOfThreads"), 0,
                                    currObject.getString("title"), currObject
                                    .getString("description"), null, null
                            )
                    );
                } else {
                    JSONObject userInfo = lastThread.optJSONObject("lastPoster");

                    if (userInfo != null) {
                        forums.add(
                                new ForumData(
                                        Long.parseLong(currObject.getString("id")), Long
                                        .parseLong(currObject.getString("categoryId")),
                                        lastThread.getLong("lastPostDate"), Long
                                        .parseLong(lastThread.getString("id")),
                                        Long.parseLong(lastThread.getString("lastPostId")), currObject
                                        .getLong("numberOfPosts"), currObject
                                        .getLong("numberOfThreads"), 0,
                                        currObject.getString("title"), currObject
                                        .getString("description"), lastThread
                                        .getString("title"), userInfo
                                        .getString("username")
                                )
                        );
                    }
                }
            }
            return new Object[]{title, forums};*/
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebsiteHandlerException("No forums found.");
        }
    }

    public ForumData getThreads(String locale, long forumId) throws WebsiteHandlerException {
        try {
            List<ForumThreadData> threads = new ArrayList<ForumThreadData>();

            final String httpContent = mRequestHandler.get(
                    RequestHandler.generateUrl(URL_FORUM, locale, forumId, 1),
                    RequestHandler.HEADER_AJAX
            );

            JSONObject contextObject = new JSONObject(httpContent).getJSONObject("context");
            JSONObject forumObject = contextObject.getJSONObject("forum");
            JSONArray stickiesArray = contextObject.getJSONArray("stickies");
            JSONArray threadArray = contextObject.getJSONArray("threads");
            int numStickies = stickiesArray.length();
            for (int i = 0, max = numStickies; i < max; i++) {
                if (i == 0) {
                    threads.add(new ForumThreadData("Stickies"));
                }

                JSONObject currObject = stickiesArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject.getJSONObject("lastPoster");

                threads.add(
                        new ForumThreadData(
                                Long.parseLong(currObject.getString("id")), forumId,
                                currObject.getLong("creationDate"), currObject
                                .getLong("lastPostDate"), currObject
                                .getInt("numberOfOfficialPosts"), currObject
                                .getInt("numberOfPosts"),
                                currObject.getString("title"),
                                new ProfileData.Builder(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username")
                                ).gravatarHash(ownerObject.getString("gravatarMd5")).build(),
                                new ProfileData.Builder(
                                        Long.parseLong(lastPosterObject.getString("userId")),
                                        lastPosterObject.getString("username")
                                ).gravatarHash(lastPosterObject.getString("gravatarMd5")).build(),
                                currObject.getBoolean("isSticky"),
                                currObject.getBoolean("isLocked")
                        )
                );
            }

            for (int i = numStickies, max = threadArray.length(); i < max; i++) {
                if (i == numStickies) {
                    threads.add(new ForumThreadData("Threads"));
                }

                JSONObject currObject = threadArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject.getJSONObject("lastPoster");

                threads.add(
                        new ForumThreadData(
                                Long.parseLong(currObject.getString("id")),
                                forumId, currObject
                                .getLong("creationDate"), currObject
                                .getLong("lastPostDate"), currObject
                                .getInt("numberOfOfficialPosts"), currObject
                                .getInt("numberOfPosts"),
                                currObject.getString("title"),

                                new ProfileData.Builder(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username")
                                ).gravatarHash(ownerObject.getString("gravatarMd5")).build(),
                                new ProfileData.Builder(
                                        Long.parseLong(lastPosterObject.getString("userId")),
                                        lastPosterObject.getString("username")
                                ).gravatarHash(lastPosterObject.getString("gravatarMd5")).build(),
                                currObject.getBoolean("isSticky"),
                                currObject.getBoolean("isLocked")
                        )
                );
            }

            return new ForumData(
                    forumObject.getString("title"),
                    forumObject.getString("description"),
                    forumObject.getLong("numberOfPosts"),
                    forumObject.getLong("numberOfThreads"),
                    contextObject.getLong("numPages"), threads
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebsiteHandlerException("No threads found.");
        }
    }

    public ForumThreadData getPosts(String locale) throws WebsiteHandlerException {
        try {
            List<ForumPostData> posts = new ArrayList<ForumPostData>();

            final String httpContent = mRequestHandler.get(
                    RequestHandler.generateUrl(URL_THREAD, locale, threadId, 1),
                    RequestHandler.HEADER_AJAX
            );

            JSONObject contextObject = new JSONObject(httpContent).getJSONObject("context");
            JSONArray postArray = contextObject.getJSONArray("posts");
            JSONObject threadObject = contextObject.getJSONObject("thread");
            JSONObject lastPosterObject = threadObject.getJSONObject("lastPoster");
            JSONObject threadOwnerObject = threadObject.getJSONObject("owner");

            for (int i = 0, max = postArray.length(); i < max; i++) {
                JSONObject currObject = postArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");

                posts.add(
                        new ForumPostData(
                                Long.parseLong(currObject.getString("id")), Long
                                .parseLong(currObject.getString("creationDate")), Long
                                .parseLong(currObject.getString("threadId")),
                                new ProfileData.Builder(
                                        Long.parseLong(ownerObject.getString("userId")),
                                        ownerObject.getString("username")
                                ).gravatarHash(ownerObject.getString("gravatarMd5")).build(),
                                currObject.getString("formattedBody"),
                                currObject.getInt("abuseCount"),
                                currObject.getBoolean("isCensored"),
                                currObject.getBoolean("isOfficial")
                        )
                );
            }

            return new ForumThreadData(
                    Long.parseLong(threadObject.getString("id")),
                    Long.parseLong(threadObject.getString("forumId")),
                    threadObject.getLong("creationDate"),
                    threadObject.getLong("lastPostDate"),
                    threadObject.getInt("numberOfOfficialPosts"),
                    threadObject.getInt("numberOfPosts"),
                    contextObject.getInt("currentPage"),
                    contextObject.getInt("numPages"),
                    threadObject.getString("title"),
                    new ProfileData.Builder(
                            Long.parseLong(threadOwnerObject.getString("userId")),
                            threadOwnerObject.getString("username")
                    ).gravatarHash(threadOwnerObject.getString("gravatarMd5")).build(),
                    new ProfileData.Builder(
                            Long.parseLong(lastPosterObject.getString("userId")),
                            lastPosterObject.getString("username")
                    ).gravatarHash(lastPosterObject.getString("gravatarMd5")).build(),
                    threadObject.getBoolean("isSticky"),
                    threadObject.getBoolean("isLocked"),
                    contextObject.getBoolean("canEditPosts"),
                    contextObject.getBoolean("canCensorPosts"),
                    contextObject.getBoolean("canDeletePosts"),
                    contextObject.getBoolean("canPostOfficial"),
                    contextObject.getBoolean("canViewLatestPosts"),
                    contextObject.getBoolean("canViewPostHistory"),
                    contextObject.getBoolean("isAdmin"), posts
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebsiteHandlerException("No thread data found.");
        }
    }

    public boolean reply(final Context c, final String body,
                         final String chksm, final ForumThreadData threadData, final boolean cache,
                         final long uid) {
        try {
            String httpContent = mRequestHandler.post(
                    RequestHandler.generateUrl(URL_POST, threadData.getId()),
                    RequestHandler.generatePostData(FIELD_NAMES_POST, body, chksm),
                    RequestHandler.HEADER_AJAX
            );

            if ("".equals(httpContent)) {
                return false;
            } else {
                if (cache) {
                    return CacheHandler.Forum.insert(c, threadData, uid) > -1;
                }
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean create(final Context c, final String topic, final String body, final String chksm) {
        try {
            String httpContent = mRequestHandler.post(
                    RequestHandler.generateUrl(URL_NEW, forumId),
                    RequestHandler.generatePostData(FIELD_NAMES_NEW, topic, body, chksm),
                    RequestHandler.HEADER_AJAX

            );
            JSONObject response = new JSONObject(httpContent);
            if (response.has("location")) {
                String[] urlBits = response.getString("location").split("/");
                boolean hasLocale = urlBits.length == 6;

                // /bf3/{locale/}forum/threadview/2832654625098756572/
                String locale = hasLocale ? urlBits[2] : "en";
                String url = hasLocale ? urlBits[5] : urlBits[4];

                threadId = Long.parseLong(url);
                ForumThreadData threadData = getPosts(locale);

                return CacheHandler.Forum.insert(c, threadData, SessionKeeper.getProfileData().getId()) > 0;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
