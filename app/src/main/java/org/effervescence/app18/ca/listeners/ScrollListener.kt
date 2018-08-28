package org.effervescence.app18.ca.listeners

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.TranslateAnimation

class ScrollListener(val view: View): RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        recyclerView?.let {
            if (it.computeVerticalScrollOffset()!=0){
                val anim = TranslateAnimation(0f,
                        0f,
                        0.25f*(0 - it.computeVerticalScrollOffset()),
                        0.25f*(0 - it.computeVerticalScrollOffset()))
                anim.fillAfter = true
                anim.duration = 0
                view.startAnimation(anim)
                view.alpha = getAlphaForView(it)
            }else{
                view.alpha = 1f
            }
        }
    }

    private fun getAlphaForView(recyclerView: RecyclerView): Float {
        return recyclerView.computeVerticalScrollOffset() * (-4.4f / recyclerView.computeVerticalScrollExtent()) + 1f

    }
}