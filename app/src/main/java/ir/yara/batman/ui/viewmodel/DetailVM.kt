package ir.yara.batman.ui.viewmodel

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import dagger.hilt.android.qualifiers.ActivityContext
import ir.yara.batman.R
import ir.yara.batman.constants.ApiConstants
import ir.yara.batman.data.remote.responce.detail.DetailListModel
import ir.yara.batman.network.api.NetworkApi
import ir.yara.batman.ui.view.customs.KitToast
import ir.yara.batman.utils.KitLog
import ir.yara.batman.utils.extension.default
import kotlinx.coroutines.launch


class DetailVM @ViewModelInject constructor(
    private val networkApi: NetworkApi,
    private val toast: KitToast,
    @ActivityContext private val context: Context
) : ViewModel() {

    var title = MutableLiveData<String>().default("")
    var year = MutableLiveData<String>().default("")
    var rated = MutableLiveData<String>().default("")
    var released = MutableLiveData<String>().default("")
    var runtime = MutableLiveData<String>().default("")
    var genre = MutableLiveData<String>().default("")
    var director = MutableLiveData<String>().default("")
    var rating = MutableLiveData<String>().default("")
    var writer = MutableLiveData<String>().default("")
    var actors = MutableLiveData<String>().default("")
    var plot = MutableLiveData<String>().default("")
    var poster = MutableLiveData<String>().default("")
    var loading = MutableLiveData<Boolean>().default(false)
    var retry = MutableLiveData<Boolean>().default(false)
    var imdbID = MutableLiveData<String>()


    init {
        getDetail()
    }


    private fun getDetail() {
        loading.value = true
        retry.value = false

        imdbID.observe(
            context as LifecycleOwner,
            object : Observer<String?> {
                override fun onChanged(imdbId: String?) {
                    viewModelScope.launch() {
                        try {
                            val response =
                                networkApi.GetDetail(imdbID = ApiConstants.DetailMovie + imdbId)
                            handleDetail(response)
                        } catch (t: Throwable) {
                            KitLog.e(t)
                            handleError(t)
                        }
                    }

                    imdbID.removeObserver(this)
                }
            })

    }

    fun getImdbID(id: String) {
        imdbID.value = id
    }

    fun retry(view: View) {
        getDetail()

    }

    private fun handleDetail(response: DetailListModel) {
        if (response.responses != "False") {
            try {
                title.value = "Title: " + response.Title
                year.value = "Year: " + response.Year
                rated.value = "Rated: " + response.Rated
                released.value = "Released: " + response.Released
                runtime.value = "Runtime: " + response.Runtime
                genre.value = "Genre: " + response.Genre
                director.value = "Director: " + response.Director
                rating.value = response.imdbRating
                writer.value = "Wirters: " + response.Writer
                actors.value = "Actors: " + response.Actors
                plot.value = "Plot: " + response.Plot
                poster.value = response.Poster
                loading.value = false
            } catch (e: Exception) {
                KitLog.e(e)
                retry.value = true
            }
        } else {
            toast.errorToast(response.error)
            retry.value = true
        }

    }


    companion object {
        @JvmStatic
        @BindingAdapter("imageUrl")
        fun loadImage(view: ImageView, url: String) {
            Glide.with(view.context)
                .load(url)
                .placeholder(R.drawable.place_holder)
                .into(view)
        }
    }


    private fun handleError(t: Throwable) {
        KitLog.e(t)
        loading.value = false
        toast.errorToast(id = R.string.error_in_connection)
        retry.value = true
    }

}