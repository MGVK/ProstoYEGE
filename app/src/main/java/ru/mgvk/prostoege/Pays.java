package ru.mgvk.prostoege;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import ru.mgvk.prostoege.util.IabHelper;
import ru.mgvk.prostoege.util.IabResult;
import ru.mgvk.prostoege.util.Inventory;
import ru.mgvk.prostoege.util.Purchase;



/**
 * Created by mihail on 08.10.16.
 */
public class Pays {

    public static final int REQUEST_CODE = 505;
    private final String SKU_22_22 = "22ez_22", SKU_250_220 = "250ez_220", SKU_1200_980 = "1200ez_980";
    private final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    Context context;
    IabHelper mHelper;
    private OnPurchaseListener listener;

    Pays(Context context) {
        this.context = context;
        initHelper();
        initMap();
    }

    private void initMap() {
        map.put(SKU_22_22, 22);
        map.put(SKU_250_220, 250);
        map.put(SKU_1200_980, 1200);
    }

    private void initHelper() {
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAslZ3KHZhVDs2N/Gvk5WHZpTQy9hiNcJeLxLMoO+FminST8IXcOp5NWoi8X7XKB5Jh+O3ZxvQ7Z6+5uuIAL5yLvq4ryvhLLrZ/d3HntmZWtqQrY4uUcjFDKqpM20AQwRr/AkiF+LJAuexukVXVEO45myUQgJwrLOTpO5D7So67o8DR4BIf3cWN3atTzO7iSLtw/NGB7gIW4eLHhoBlt344uUYujGQp5yXcszTAAl9FIXjHHMCDtf0rP03zowfxo1rBlT8XSIRSkV0pojJCmbHd4bWLh9sQg1ZvoY7IXSip6b70TDtQTf5VeIXwtC0kriQr0mDAB9GL6hi5BG8MY3rOwIDAQAB";
        mHelper = new IabHelper(context, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                inventoryQuery();
            }
        });
    }

    void inventoryQuery() {
        try {
            final ArrayList<String> l
                    = new ArrayList<>(map.keySet());
//            =new ArrayList<>();
//            l.add(SKU_22_22);

            mHelper.queryInventoryAsync(true,l,l,new IabHelper.QueryInventoryFinishedListener() {
                @Override
                public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                    if (result.isFailure()) {
                        Log.d("fail", "" + result.getMessage());
                        ((MainActivity) context).ui.makeErrorMessage(
                                "При проверке покупок произошла ошибка! \n" +
                                        "Код ошибки: " + result.getResponse());
                    } else {

                        for (final String s :map.keySet()) {
                            Log.d("sku", s);
                            try {
                                if (inv.getPurchase(s) != null)
                                    mHelper.consumeAsync(inv.getPurchase(s), new IabHelper.OnConsumeFinishedListener() {
                                        @Override
                                        public void onConsumeFinished(Purchase purchase, IabResult result) {
                                            Log.d("Result", "" + result);
                                            try {
                                                DataLoader.buyCoins(map.get(s));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            ((MainActivity) context).updateCoins(map.get(s));
                                            success = true;
                                        }
                                    });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        success = true;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void donate(final String SKU) {

        success = false;
        try {
            mHelper.launchPurchaseFlow(((MainActivity) context), SKU, REQUEST_CODE, new IabHelper.OnIabPurchaseFinishedListener() {
                public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

                    Log.d("Purchase", "Finished");

                    if (result.isSuccess()) {
                        Log.d("PurchasingSuccess", "" + result.getMessage());

                        try {
//                            DataLoader.buyCoins(map.get(purchase.getSku()));
//                            ((MainActivity) context).ui.updateCoins();
//
//                            mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
//                                @Override
//                                public void onConsumeFinished(Purchase purchase, IabResult result) {
//                                    Log.d("comsuming", "" + result.getMessage());
//                                }
//                            });
                            inventoryQuery();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("PurchaseError", result.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

//        mHelper.disposeWhenFinished();

        if(!success) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!success) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                inventoryQuery();
                            }
                        });

                    }
                }
            })/*.start()*/;
        }
    }

    boolean success=false;

    public void buyPack(int index) throws IabHelper.IabAsyncInProgressException {
        Log.d("Pays", "Index = " + index);
        switch (index) {
            case 0: {
                donate(SKU_22_22);
                break;
            }
            case 1: {
                donate(SKU_250_220);
                break;
            }
            case 2: {
                donate(SKU_1200_980);
                break;
            }

        }
    }


    public String buyVideo(int id) throws Exception {
         return DataLoader.buyVideo(id);
    }

    public boolean buyHint(int id) {
        try {
            return DataLoader.buyHint(id).equals("1");
        } catch (Exception e) {
            return false;
        }
    }

    public void setOnPurchaseListener(OnPurchaseListener listener) {
        this.listener = listener;
    }
    public interface OnPurchaseListener {
        public void OnPurchase();
    }
}
