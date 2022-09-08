package com.ranseo.solaroid.custom.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class FloatingActionButtonBehavior(context: Context, attributeSet: AttributeSet)
    : CoordinatorLayout.Behavior<View>(context,attributeSet) {

    /**
     * 레이아웃에 트리거가 발생하면 호출
     * true를 return 하면 onDependentViewChanged를 부른다.
     * */
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    /**
     * dependency의 view의 변화가 있을 때 이벤트가 들어온다.
     * */
    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if(parent==null || child ==null || dependency == null ) return false
        val translationY = getViewOffsetForSnackbar(parent, child)
        val fractionComplete = translationY / dependency.height
        val scaleFactor = 1 - fractionComplete

        child.scaleX = scaleFactor
        child.scaleY = scaleFactor

        return true
    }

    private fun getViewOffsetForSnackbar(parent: CoordinatorLayout, view: View): Float{
        var maxOffset = 0f
        val dependencies = parent.getDependencies(view)

        dependencies.forEach { dependency ->
            if (dependency is Snackbar.SnackbarLayout && parent.doViewsOverlap(view, dependency)){
                maxOffset = Math.max(maxOffset, (dependency.translationY - dependency.height) * -1)
            }
        }

        return maxOffset
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        child.scaleX = 1f
        child.scaleY = 1f
    }
}