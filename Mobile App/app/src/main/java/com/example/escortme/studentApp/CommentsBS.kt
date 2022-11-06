package com.example.escortme.studentApp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.escortme.R
import com.example.escortme.network.RetrofitClient
import com.example.escortme.studentApp.news.AlertAdapter
import com.example.escortme.studentApp.news.Comment
import com.example.escortme.studentApp.news.CommentsAdapter
import com.example.escortme.studentApp.ui.home.HomeFragment
import com.example.escortme.utils.Helpers
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class CommentsBS  : BottomSheetDialogFragment() {
    private var alertId: Long = 0
    lateinit var commentsList: List<Comment>
    var commentsAdapter: CommentsAdapter? = null
    lateinit var commentsRV: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.comments_bottomsheet, container, false)

        commentsRV = view.findViewById(R.id.rvComments)



        alertId = AlertAdapter.alertId;
        val studentId: Long = HomeFragment.studentIdLong
        println("ALERT ID $alertId")
        retrieveComments()


        val addNewComment : MaterialCardView = view.findViewById(R.id.sendComment);
        val userComment : TextInputEditText = view.findViewById(R.id.userComment);


        addNewComment.setOnClickListener {
            val content : String = userComment.text.toString()
            if(content != null){
                println("USER COMMENT $content" )
                Helpers.addComment(alertId,studentId, content, activity)
                userComment.text?.clear()
                retrieveComments()
            }
        }

        return view
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                heightSetup(it)
                behaviour.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED);

        return dialog
    }

    private fun retrieveComments(){
        val allComments = RetrofitClient.getRetrofitClient().api.getAllCommentsByAlertId(alertId)
        allComments.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    try {
                        assert(response.body() != null)
                        val data = response.body()!!.string()
                        commentsList = ArrayList()
                        val jsonArray = JSONArray(data)
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val id = jsonObject.getLong("id")
                            val username = jsonObject.getString("username")
                            val commentJsonObj = jsonObject.getJSONObject("comment")
                            val content = commentJsonObj.getString("content")
                            val date = commentJsonObj.getString("dateCreated")
                            (commentsList as ArrayList<Comment>).add(Comment(id, content, username, date))
                        }
                        commentsAdapter = CommentsAdapter(commentsList)
                        commentsAdapter!!.notifyDataSetChanged()
                        commentsAdapter!!.notifyItemInserted(0)
                        commentsRV.adapter = commentsAdapter
                        commentsRV.layoutManager = LinearLayoutManager(context)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    Helpers.failure(activity, "Failed to retrieve comments")
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Helpers.failure(activity, "Server error")
            }
        })
    }

    private fun heightSetup(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }


}