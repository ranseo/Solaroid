package com.ranseo.solaroid.custom.behavior

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class FloatingActionButtonBehavior : CoordinatorLayout.Behavior<FloatingActionButton>() {

    /**
     * 레이아웃에 트리거가 발생하면 호출
     * true를 return 하면 onDependentViewChanged를 부른다.
     * */
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View
    ): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    /**
     * dependency의 view의 변화가 있을 때 이벤트가 들어온다.
     * */
    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View
    ): Boolean {

        val translationY = Math.min(0F, dependency.translationY - dependency.height)
        val percentComplete = -translationY / dependency.height
        val scaleFactor = 1 - percentComplete

        child.scaleX = scaleFactor
        child.scaleY = scaleFactor

        return false
    }
}