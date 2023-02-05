package jp.techacademy.koji.tanno.taskapp

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemClickListener(context: Context, recyclerView: RecyclerView, private val listener: RecyclerItemClickListener.OnRecyclerClickListener): RecyclerView.SimpleOnItemTouchListener() {

        interface OnRecyclerClickListener{
            fun onItemClick(view: View, position: Int)
            fun onDoubleClick(view: View, position: Int)
            fun onItemLongClick(view: View, position: Int)
        }

        private val gestureDetector = GestureDetectorCompat(context, object: GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {

                val childView = recyclerView.findChildViewUnder(e.x, e.y)

                if (childView != null) {
                    listener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView))
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val childView = recyclerView.findChildViewUnder(e.x, e.y)

                if (childView != null) {
                    listener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView))
                }
                super.onLongPress(e)
            }
        })

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            Log.d("MAIN","onInterceptTouchEvent called")
            val result = gestureDetector.onTouchEvent(e)
            return result
        }
    }