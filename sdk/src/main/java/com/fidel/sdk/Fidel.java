package com.fidel.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

/**
 * Created by reenboog on 7/25/17.
 */

public class Fidel {
    public static final String FIDEL_DEBUG_TAG = "Fidel.DEBUG";
    public static int FIDEL_LINK_CARD_REQUEST_CODE = 1624;
    public static String FIDEL_LINK_CARD_RESULT_CARD = "card";

    private static String decoded(String s) throws UnsupportedEncodingException {
        byte[] decrypt = Base64.decode(s, Base64.DEFAULT);
        String text = new String(decrypt, StandardCharsets.UTF_8);

        return text;
    }

    private static boolean acceptsTOS() {
        return 5 == (3 + 2);
    }

    private static String API_ROOT = "aHR0cHM6Ly9hcGkuZmlkZWwudWsvdjE="; // "https://api.fidel.uk/v1"
    private static String API_PROGRAMS = "cHJvZ3JhbXM="; // "programs"
    private static String API_CARDS = "Y2FyZHM="; // cards

    /**
     * an optional banner image shown at the top of the form
     */
    public static Bitmap bannerImage = null;

    /**
     * when set to true, automatically starts CardIO card scanning activity
     */
    public static boolean autoScan = false;

    /**
     * required for a linkCard() call; get it from your dashboard
     */
    public static String programId = null;

    /**
     * required for a linkCard() call; get it from your dashboard
     */
    public static String apiKey = null;

    /**
     * optionally used in linkCard();
     */
    public static JsonObject metaData = null;

    public interface OnCardOperationDelegate {
        void onCardLinked(LinkResult resultCard);
        void onFailedToLinkCard(String error);
    }

    public static void linkCard(EnterCardDetailsActivity.FidelServiceAuthorization authorization,
                                String card,
                                int expMonth,
                                int expYear,
                                String countryCode,
                                final WeakReference<OnCardOperationDelegate> cardDelegate,
                                final WeakReference<AppCompatActivity> weakActivity) throws UnsupportedEncodingException {

        linkCardImpl(authorization, card, expMonth, expYear, countryCode, cardDelegate, weakActivity);
    }

    private static void linkCardImpl(EnterCardDetailsActivity.FidelServiceAuthorization authorization,
                                    String card,
                                    int expMonth,
                                    int expYear,
                                    String countryCode,
                                    final WeakReference<OnCardOperationDelegate> cardDelegate,
                                    final WeakReference<AppCompatActivity> weakActivity) throws UnsupportedEncodingException {

        authorization.isAuthorized();

        class FidelErrorHandler {
            void fail(final String msg) {

                final AppCompatActivity activity = weakActivity.get();
                final OnCardOperationDelegate delegate = cardDelegate.get();

                Log.d(FIDEL_DEBUG_TAG, msg);

                if(activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(delegate != null) {
                                delegate.onFailedToLinkCard(msg);
                            }
                        }
                    });
                }
            }
        }

        final FidelErrorHandler onError = new FidelErrorHandler();

        if(programId == null || programId.isEmpty()) {
            onError.fail("You need to specify a programId first.");
            return;
        }

        if(!ExpiryDateUtil.isExpiryMonthValid(expMonth)) {
            onError.fail("Expiry month is incorrect: " + expMonth);
            return;
        }

        if(ExpiryDateUtil.isExpiryYearInTwoDigitFormat(expYear)) {
            onError.fail("Expiry year is expected to be in 4-digit format and > " + ExpiryDateUtil.MILLENNIUM_BASE + ".");
            return;
        }

        String urlStr = decoded(API_ROOT) + "/" + decoded(API_PROGRAMS) + "/" + programId + "/" + decoded(API_CARDS);

        JsonObject jsonParams = new JsonObject();

        jsonParams.addProperty(decoded("ZXhwTW9udGg="), expMonth); // expMonth
        jsonParams.addProperty(decoded("ZXhwWWVhcg=="), expYear); // expYear
        jsonParams.addProperty(decoded("Y291bnRyeUNvZGU="), countryCode); // countryCode
        jsonParams.addProperty(decoded("bnVtYmVy"), card); // number
        jsonParams.addProperty(decoded("dGVybXNPZlVzZQ=="), acceptsTOS()); // termsOfUse

        if(metaData != null) {
            jsonParams.add(decoded("bWV0YWRhdGE="), metaData); // metadata
        }

        Ion.with(weakActivity.get())
                .load("POST", urlStr)
                .setHeader(decoded("ZmlkZWwta2V5"), apiKey) // fidel-key
                .setHeader(decoded("Q29udGVudC10eXBl"), decoded("YXBwbGljYXRpb24vanNvbg==")) // Content-type, application/json
                .setHeader(decoded("QWNjZXB0"), decoded("YXBwbGljYXRpb24vanNvbg==")) // Accept, application/json
                .setTimeout(10 * 1000)
                .setJsonObjectBody(jsonParams)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {

                        final boolean success = (e == null) && (result != null);

                        if(!success) {
                            String localizedError = e.getLocalizedMessage();
                            String httpError =  localizedError != null ? localizedError : "Something went wrong while performing an http POST request.";

                            onError.fail(httpError);
                            return;
                        } else {
                            JsonObject jsonError = result.getAsJsonObject("error");

                            if(jsonError != null) {
                                String jsonErrorMessage = jsonError.get("message").getAsString();

                                onError.fail(jsonErrorMessage);
                                return;
                            } else {
                                JsonArray jsonItems = result.getAsJsonArray("items");

                                if(jsonItems == null) {
                                    onError.fail("No items field found in response");
                                    return;
                                } else {
                                    JsonObject cardObj = jsonItems.get(0).getAsJsonObject();
                                    String cardId = cardObj.get("id").getAsString();

                                    if(cardId != null) {
                                        final LinkResult resultCard = new LinkResult(cardId);

                                        // fill it in here
                                        if(cardObj.get("created") != null) {
                                            resultCard.created = cardObj.get("created").getAsString();
                                        }

                                        if(cardObj.get("updated") != null) {
                                            resultCard.updated = cardObj.get("updated").getAsString();
                                        }

                                        if(cardObj.get("type") != null) {
                                            resultCard.type = cardObj.get("type").getAsString();
                                        }

                                        if(cardObj.get("scheme") != null) {
                                            resultCard.scheme = cardObj.get("scheme").getAsString();
                                        }

                                        if(cardObj.get("programId") != null) {
                                            resultCard.programId = cardObj.get("programId").getAsString();
                                        }

                                        if(cardObj.get("mapped") != null) {
                                            resultCard.mapped = cardObj.get("mapped").getAsBoolean();
                                        }

                                        if(cardObj.get("live") != null) {
                                            resultCard.live = cardObj.get("live").getAsBoolean();
                                        }

                                        if(cardObj.get("lastNumbers") != null) {
                                            resultCard.lastNumbers = cardObj.get("lastNumbers").getAsString();
                                        }

                                        if(cardObj.get("expYear") != null) {
                                            resultCard.expYear = cardObj.get("expYear").getAsInt();
                                        }

                                        if(cardObj.get("expMonth") != null) {
                                            resultCard.expMonth = cardObj.get("expMonth").getAsInt();
                                        }

                                        if(cardObj.get("expDate") != null) {
                                            resultCard.expDate = cardObj.get("expDate").getAsString();
                                        }

                                        if(cardObj.get("countryCode") != null) {
                                            resultCard.countryCode = cardObj.get("countryCode").getAsString();
                                        }

                                        if(cardObj.get("accountId") != null) {
                                            resultCard.accountId = cardObj.get("accountId").getAsString();
                                        }

                                        if(cardObj.get("metadata") != null) {
                                            resultCard.metaData = cardObj.get("metadata").getAsJsonObject();
                                        }

                                        //

                                        final OnCardOperationDelegate delegate = cardDelegate.get();
                                        final AppCompatActivity activity = weakActivity.get();

                                        if(activity != null) {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(delegate != null) {
                                                        delegate.onCardLinked(resultCard);
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        onError.fail("No id field found in response");
                                    }

                                }
                            }
                        }
                    }
                });
    }

    /**
     * @param src is just an activity on top of which EnterCardDetails will be shown modally
     */

    public static void present(AppCompatActivity src) {
        Intent intent = new Intent(src, EnterCardDetailsActivity.class);

        src.startActivityForResult(intent, FIDEL_LINK_CARD_REQUEST_CODE);
    }
}
