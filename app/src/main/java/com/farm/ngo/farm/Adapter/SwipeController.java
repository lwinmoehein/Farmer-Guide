package com.farm.ngo.farm.Adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;


import com.farm.ngo.farm.R;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;

enum ButtonsState {

        GONE,

        LEFT_VISIBLE,

        RIGHT_VISIBLE

}



public class SwipeController extends ItemTouchHelper.Callback {



        private boolean swipeBack = false;



        private ButtonsState buttonShowedState = ButtonsState.GONE;



        private RectF buttonInstance = null;



        private RecyclerView.ViewHolder currentItemViewHolder = null;



        private SwipeControllerActions buttonsActions = null;



        private static final float buttonWidth = 150;

        private Context context;



        public SwipeController(Context context) {
            this.context=context;
        }

        public void setSwipeAction(SwipeControllerActions action){
                this.buttonsActions=action;
        }



        @Override

        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                return makeMovementFlags(0,  LEFT);

        }



        @Override

        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

                return false;

        }



        @Override

        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {



        }



        @Override

        public int convertToAbsoluteDirection(int flags, int layoutDirection) {

                if (swipeBack) {

                        swipeBack = buttonShowedState != ButtonsState.GONE;

                        return 0;

                }

                return super.convertToAbsoluteDirection(flags, layoutDirection);

        }



        @Override

        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (actionState == ACTION_STATE_SWIPE) {

                        if (buttonShowedState != ButtonsState.GONE) {

                                if (buttonShowedState == ButtonsState.LEFT_VISIBLE) dX = Math.max(dX, buttonWidth);

                                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) dX = Math.min(dX, -buttonWidth);

                                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                        }

                        else {

                                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                        }

                }



                if (buttonShowedState == ButtonsState.GONE) {

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                }

                currentItemViewHolder = viewHolder;
                drawButtons(c,viewHolder);

        }



        private void setTouchListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {

                recyclerView.setOnTouchListener(new View.OnTouchListener() {

                        @Override

                        public boolean onTouch(View v, MotionEvent event) {

                                swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;

                                if (swipeBack) {

                                        if (dX < -buttonWidth) buttonShowedState = ButtonsState.RIGHT_VISIBLE;

                                        else if (dX > buttonWidth) buttonShowedState  = ButtonsState.LEFT_VISIBLE;



                                        if (buttonShowedState != ButtonsState.GONE) {

                                                setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                                                setItemsClickable(recyclerView, false);

                                        }

                                }

                                return false;

                        }

                });

        }



        private void setTouchDownListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {

                recyclerView.setOnTouchListener(new View.OnTouchListener() {

                        @Override

                        public boolean onTouch(View v, MotionEvent event) {

                                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                                        setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                                }

                                return false;

                        }

                });

        }



        private void setTouchUpListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {

                recyclerView.setOnTouchListener(new View.OnTouchListener() {

                        @Override

                        public boolean onTouch(View v, MotionEvent event) {

                                if (event.getAction() == MotionEvent.ACTION_UP) {

                                        SwipeController.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);

                                        recyclerView.setOnTouchListener(new View.OnTouchListener() {

                                                @Override

                                                public boolean onTouch(View v, MotionEvent event) {

                                                        return false;

                                                }

                                        });

                                        setItemsClickable(recyclerView, true);

                                        swipeBack = false;



                                        if (buttonsActions != null && buttonInstance != null && buttonInstance.contains(event.getX(), event.getY())) {

                                               if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {

                                                        buttonsActions.onRightClicked(viewHolder.getAdapterPosition());

                                                }

                                        }

                                        buttonShowedState = ButtonsState.GONE;

                                        currentItemViewHolder = null;

                                }

                                return false;

                        }

                });

        }



        private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {

                for (int i = 0; i < recyclerView.getChildCount(); ++i) {

                        recyclerView.getChildAt(i).setClickable(isClickable);

                }

        }



        private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {

                float buttonWidthWithoutPadding = buttonWidth - 10;

                float corners = 8;



                View itemView = viewHolder.itemView;

                Paint p = new Paint();


                RectF rightButton = new RectF(itemView.getRight() - buttonWidthWithoutPadding, itemView.getTop(), itemView.getRight(), itemView.getBottom());

                p.setColor(context.getResources().getColor(R.color.delete_back));

                c.drawRoundRect(rightButton, corners, corners, p);

                drawText("DELETE", c, rightButton, p);



                buttonInstance = null;
                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {

                        buttonInstance = rightButton;

                }

        }



        private void drawText(String text, Canvas c, RectF button, Paint p) {

                float textSize = 30;

                p.setColor(Color.WHITE);

                p.setAntiAlias(true);

                p.setTextSize(textSize);
                p.setTypeface(Typeface.DEFAULT_BOLD);

                float textWidth = p.measureText(text);

                c.drawText(text, button.centerX()-(textWidth/2), button.centerY()+(textSize/2), p);

        }



        public void onDraw(Canvas c) {

                if (currentItemViewHolder != null) {

                        drawButtons(c, currentItemViewHolder);

                }

        }

        public abstract static class SwipeControllerActions {
            public void onRightClicked(int position) {}
        }
}