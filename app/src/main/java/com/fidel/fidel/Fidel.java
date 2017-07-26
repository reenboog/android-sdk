package com.fidel.fidel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.lang.ref.WeakReference;

/**
 * Created by reenboog on 7/25/17.
 */

public class Fidel {
    public static int FIDEL_LINK_CARD_REQUEST_CODE = 1624;
    public static String FIDEL_LINK_CARD_RESULT_CARD_ID = "cardId";

    private static String API_ROOT = "https://api.fidel.uk/v1";
    private static String API_PROGRAMS = "programs";
    private static String API_CARDS = "cards";

    public static Bitmap bannerImage = null;
    public static boolean autoScan = false;
    public static String programId = null;
    public static String apiKey = null;

    public interface OnCardOperationDelegate {
        void onCardLinked(String cardId);
        void onFailedToLinkCard(String error);
    }

    public static void linkCard(EnterCardDetailsActivity.FidelServiceAuthorization authorization,
                                String card,
                                int expMonth,
                                int expYear,
                                String countryCode,
                                final WeakReference<EnterCardDetailsActivity> cardDelegate) {

        authorization.isAuthorized();

        class FidelErrorHandler {
            void fail(final String msg) {

                final EnterCardDetailsActivity delegate = cardDelegate.get();

                Log.d("Fidel.DEBUG", msg);

                if(delegate != null) {
                    delegate.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            delegate.onFailedToLinkCard(msg);
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

        if(expMonth < 1 || expMonth > 12) {
            onError.fail("Expiry month is incorrect: " + expMonth);
            return;
        }

        if(expYear < ExpiryDateUtil.MILLENIUM_BASE) {
            onError.fail("Expiry year is expected to be in 4-digit format and > " + ExpiryDateUtil.MILLENIUM_BASE + ".");
            return;
        }

        String urlStr = API_ROOT + "/" + API_PROGRAMS + "/" + programId + "/" + API_CARDS;

        JsonObject jsonParams = new JsonObject();

        jsonParams.addProperty("expMonth", expMonth);
        jsonParams.addProperty("expYear", expYear);
        jsonParams.addProperty("countryCode", countryCode);
        jsonParams.addProperty("number", card);
        jsonParams.addProperty("termsOfUse", true);

        EnterCardDetailsActivity activity = cardDelegate.get();

        Ion.with(activity)
                .load("POST", urlStr)
                .setHeader("fidel-key", apiKey)
                .setHeader("Content-type", "application/json")
                .setHeader("Accept", "application/json")
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
                                    final String cardId = jsonItems.get(0).getAsJsonObject().get("id").getAsString();

                                    if(cardId != null) {
                                        final EnterCardDetailsActivity delegate = cardDelegate.get();

                                        if(delegate != null) {
                                            delegate.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    delegate.onCardLinked(cardId);
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


    public static void present(Activity src) {
        Intent intent = new Intent(src, EnterCardDetailsActivity.class);

        src.startActivityForResult(intent, FIDEL_LINK_CARD_REQUEST_CODE);
    }
}
