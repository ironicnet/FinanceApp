/* Class that handles the Card Notification View seen in the Home Screen
 * Sets up the app and displays the cards notifying the user of important events
 */

package com.databases.example.app;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.databases.example.R;
import com.databases.example.data.DatabaseHelper;
import com.databases.example.data.DateTime;
import com.databases.example.data.MyCard;
import com.databases.example.data.MyContentProvider;
import com.databases.example.data.MyPlayCard;
import com.fima.cardsui.objects.Card;
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Cards extends SherlockFragment {
    private static CardUI mCardView;
    public static boolean accountChanged = false;
    public static boolean transactionChanged = false;
    public static boolean planChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }// end onCreate

    @Override
    public void onResume() {
        super.onResume();
        if (accountChanged || planChanged || transactionChanged) {
            Log.d("Cards", "Refreshing Cards...");
            mCardView.clearCards();
            dealCardsCheckbook(mCardView);
            dealCardsPlans(mCardView);
            accountChanged = false;
            planChanged = false;
            transactionChanged = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myFragmentView = inflater.inflate(R.layout.cards_ui, container, false);

        //Initialize Card View
        mCardView = (CardUI) myFragmentView.findViewById(R.id.cardsview);
        mCardView.setSwipeable(true);

        dealCardsCheckbook(mCardView);
        dealCardsPlans(mCardView);
        //dealCardsStatistics(mCardView);

        accountChanged = false;
        planChanged = false;
        transactionChanged = false;

        return myFragmentView;
    }

    private void dealCardsCheckbook(CardUI view) {
        Cursor accountCursor = getActivity().getContentResolver().query(MyContentProvider.ACCOUNTS_URI, null, null, null, null);
        Cursor transactionCursor = getActivity().getContentResolver().query(MyContentProvider.TRANSACTIONS_URI, null, null, null, null);

        CardTaskAccounts taskAccount = new CardTaskAccounts();
        taskAccount.execute(accountCursor);

        CardTaskTransactions taskTransaction = new CardTaskTransactions();
        taskTransaction.execute(transactionCursor);
    }

    private void dealCardsPlans(CardUI view) {
        Cursor planCursor = getActivity().getContentResolver().query(MyContentProvider.PLANS_URI, null, null, null, null);

        CardTaskPlans runner = new CardTaskPlans();
        runner.execute(planCursor);
    }

    public void dealCardsStatistics(CardUI view) {
        //CardTask runner = new CardTask();
        //runner.execute("Statistics",view);
    }

    private class CardTaskAccounts extends AsyncTask<Object, Void, ArrayList<Card>> {

        @Override
        protected ArrayList<Card> doInBackground(Object... params) {
            final Cursor cursor = (Cursor) params[0];
            ArrayList<Card> cards = new ArrayList<Card>();

            final int account_name_column = cursor.getColumnIndex(DatabaseHelper.ACCOUNT_NAME);
            final int account_balance_column = cursor.getColumnIndex(DatabaseHelper.ACCOUNT_BALANCE);
            String account_name;
            String account_balance;

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final boolean onlyOverdrawn = prefs.getBoolean("checkbox_card_accountOnlyOverdrawn", false);

            String title;
            String description;
            String color;

            while (cursor.moveToNext() && !isCancelled()) {
                title = "";
                description = "";
                color = "";

                account_name = cursor.getString(account_name_column);
                account_balance = cursor.getString(account_balance_column);

                //Determine if Account health is good or not
                if (Float.parseFloat(account_balance) >= 0 && !onlyOverdrawn) {
                    title = account_name;
                    description = "This account is doing well.";
                    color = "#4ac925";
                } else if (Float.parseFloat(account_balance) < 0) {
                    title = account_name;
                    description = "This account is overdrawn.";
                    color = "#e00707";
                }

                if (title.length() > 0) {
                    cards.add(new MyPlayCard(title, description, color, "#222222", false, false));
                }

            }//end while

            return cards;
        }

        @Override
        protected void onPostExecute(ArrayList<Card> result) {
            CardStack stackCheckbook = new CardStack();
            stackCheckbook.setTitle("CHECKBOOK");
            mCardView.addStack(stackCheckbook);
            int count = 0;

            for (Card item : result) {
                if (count == 0) {
                    mCardView.addCard(item);
                } else {
                    mCardView.addCardToLastStack(item);
                }

                count++;
            }

            if (count == 0) {
                String title = "No Accounts";
                String description = "No Accounts created yet";
                mCardView.addCard(new MyCard(title, description));
            }

            mCardView.refresh();
        }
    }

    private class CardTaskTransactions extends AsyncTask<Object, Void, ArrayList<Card>> {

        @Override
        protected ArrayList<Card> doInBackground(Object... params) {
            final Cursor cursor = (Cursor) params[0];
            ArrayList<Card> cards = new ArrayList<Card>();

            final int transaction_name_column = cursor.getColumnIndex(DatabaseHelper.TRANS_NAME);
            final int transaction_date_column = cursor.getColumnIndex(DatabaseHelper.TRANS_DATE);
            final int transaction_cleared_column = cursor.getColumnIndex(DatabaseHelper.TRANS_CLEARED);

            String transaction_name;
            DateTime transaction_date = new DateTime();
            String transaction_cleared;

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final int daysRecent = Integer.parseInt(prefs.getString("pref_key_card_transactionDaysRecent", "5"));

            String title;
            String description;
            String color;
            long difference;

            final Date today_date = new Date();

            while (cursor.moveToNext() && !isCancelled()) {
                title = "";
                description = "";
                color = "";
                difference = 0;

                transaction_name = cursor.getString(transaction_name_column);
                //transaction_date = new DateTime ();
                transaction_date.setStringSQL(cursor.getString(transaction_date_column));
                transaction_cleared = cursor.getString(transaction_cleared_column);

                //Calculate difference of dates
                try {
                    difference = (today_date.getTime() - transaction_date.getYearMonthDay().getTime()) / 86400000;
                    Log.d("Cards", transaction_name + " Difference=" + difference);
                } catch (ParseException e) {
                    Log.e("Cards", "Error parsing transaction time? e=" + e);
                    e.printStackTrace();
                }

                //Uncleared transactions
                if (!Boolean.parseBoolean(transaction_cleared)) {
                    title = transaction_name;
                    description = "This transaction has not been cleared.";
                    color = "#f2a400";
                }

                //Recent transactions
                if (Math.abs(difference) < daysRecent) {
                    title = transaction_name;
                    color = "#f2a400";

                    switch (new BigDecimal(difference).intValueExact()) {
                        case 0:
                            description = "This transaction occured today.";
                            break;
                        case 1:
                            description = "This transaction occured yesterday.";
                            break;
                        default:
                            description = "This transaction occured " + difference + " days ago";
                            break;
                    }

                }

                if (title.length() > 0) {
                    cards.add(new MyPlayCard(title, description, color, "#222222", false, false));
                }

            }//end while

            return cards;
        }

        @Override
        protected void onPostExecute(ArrayList<Card> result) {
            int count = 0;

            for (Card item : result) {
                if (count == 0) {
                    mCardView.addCard(item);
                } else {
                    mCardView.addCardToLastStack(item);
                }

                count++;
            }

            if (count == 0) {
                String title = "No Transactions";
                String description = "No Transactions have occured recently";
                mCardView.addCard(new MyCard(title, description));
            }

            mCardView.refresh();
        }
    }

    private class CardTaskPlans extends AsyncTask<Object, Void, ArrayList<Card>> {

        @Override
        protected ArrayList<Card> doInBackground(Object... params) {
            Cursor cursor = (Cursor) params[0];
            ArrayList<Card> cards = new ArrayList<Card>();

            String plan_name;
            String plan_offset;
            String plan_rate;
            String plan_scheduled;
            Date d = null;
            DateTime test = new DateTime();
            final Date today_date = new Date();
            Calendar firstRun;
            String title;
            String description;
            String color;
            long difference;

            final int plan_name_column = cursor.getColumnIndex(DatabaseHelper.PLAN_NAME);
            final int plan_offset_column = cursor.getColumnIndex(DatabaseHelper.PLAN_OFFSET);
            final int plan_rate_column = cursor.getColumnIndex(DatabaseHelper.PLAN_RATE);
            final int plan_scheduled_column = cursor.getColumnIndex(DatabaseHelper.PLAN_SCHEDULED);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final int lookAhead = Integer.parseInt(prefs.getString("pref_key_card_planLookAhead", "5"));

            while (cursor.moveToNext() && !isCancelled()) {
                title = "";
                description = "";
                color = "";

                plan_name = cursor.getString(plan_name_column);
                plan_offset = cursor.getString(plan_offset_column);
                plan_rate = cursor.getString(plan_rate_column);
                plan_scheduled = cursor.getString(plan_scheduled_column);

                try {
                    test.setStringSQL(plan_offset);
                    d = test.getYearMonthDay();
                } catch (java.text.ParseException e) {
                    Log.e("Cards", "Couldn't grab date for " + plan_name + "\n e:" + e);
                }

                //Parse Rate (token 0 is amount, token 1 is type)
                final String delims = "[ ]+";
                final String[] tokens = plan_rate.split(delims);

                firstRun = new GregorianCalendar(d.getYear() + 1900, d.getMonth(), d.getDate());

                if (tokens[1].contains("Days")) {
                    //If Starting Time is in the past, fire off next day(s)
                    while (firstRun.before(Calendar.getInstance())) {
                        firstRun.add(Calendar.DAY_OF_MONTH, Integer.parseInt(tokens[0]));
                    }
                } else if (tokens[1].contains("Weeks")) {
                    //If Starting Time is in the past, fire off next week(s)
                    while (firstRun.before(Calendar.getInstance())) {
                        firstRun.add(Calendar.WEEK_OF_MONTH, Integer.parseInt(tokens[0]));
                    }
                } else if (tokens[1].contains("Months")) {
                    //If Starting Time is in the past, fire off next month(s)
                    while (firstRun.before(Calendar.getInstance())) {
                        firstRun.add(Calendar.MONTH, Integer.parseInt(tokens[0]));
                    }
                }

                difference = (today_date.getTime() - firstRun.getTimeInMillis()) / 86400000;
                Log.d("Cards", plan_name + " Difference=" + difference);

                //Recent plans
                if (Math.abs(difference) < lookAhead && plan_scheduled.equals("true")) {
                    title = plan_name;
                    color = "#33b6ea";

                    if (difference == 0) {
                        description = "This planned transaction occured today";
                    } else if (difference == 1) {
                        description = "This planned transaction occured yesterday";
                    } else if (difference == -1) {
                        description = "This planned transaction is coming up tomorrow";
                    } else if (difference < -1) {
                        description = "This planned transaction occured recently";
                    } else if (difference > 1) {
                        description = "This planned transaction is coming up";
                    }
                }

                //Title only assigned if it meets criteria
                if (title.length() > 0) {
                    cards.add(new MyPlayCard(title, description, color, "#222222", false, false));
                }

            }//end while

            return cards;
        }

        @Override
        protected void onPostExecute(ArrayList<Card> result) {
            CardStack stackPlans = new CardStack();
            stackPlans.setTitle("PLANS");
            mCardView.addStack(stackPlans);
            int count = 0;

            for (Card item : result) {
                if (count == 0) {
                    mCardView.addCard(item);
                } else {
                    mCardView.addCardToLastStack(item);
                }

                count++;
            }

            if (count == 0) {
                String title = "No Plans";
                String description = "No Plans are coming up";
                mCardView.addCard(new MyCard(title, description));
            }

            mCardView.refresh();
        }
    }

}// end Cards