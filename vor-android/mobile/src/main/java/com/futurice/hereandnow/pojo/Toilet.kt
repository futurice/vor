package com.futurice.hereandnow.pojo

import android.content.SharedPreferences

/**
 * Created by Luís Ramalho on 28/12/15.
 * <luis.ramalho@futurice.com>
 */
class Toilet {
    var floor: Int? = null
    var id: String? = null
    var status: Boolean? = null
    var sharedPreferences: SharedPreferences? = null

    constructor(floor: Int?, id: String) {
        this.floor = floor
        this.id = id
    }
}
