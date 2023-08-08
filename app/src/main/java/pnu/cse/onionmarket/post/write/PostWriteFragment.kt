package pnu.cse.onionmarket.post.write

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import pnu.cse.onionmarket.MainActivity
import pnu.cse.onionmarket.R
import pnu.cse.onionmarket.databinding.FragmentPostWriteBinding
import pnu.cse.onionmarket.databinding.ItemWriteImageBinding
import pnu.cse.onionmarket.home.HomeFragmentDirections
import pnu.cse.onionmarket.home.HomePostAdapter
import pnu.cse.onionmarket.post.PostItem
import pnu.cse.onionmarket.post.write.WriteImageAdapter.Companion.imageCount
import java.util.UUID

class PostWriteFragment : Fragment(R.layout.fragment_post_write) {
    private lateinit var binding: FragmentPostWriteBinding
    private lateinit var writeImageAdapter: WriteImageAdapter
    private lateinit var homePostAdapter: HomePostAdapter

    private var imageList: MutableList<Uri> = mutableListOf()
    private var postId = UUID.randomUUID().toString()
    private val writerId = Firebase.auth.currentUser?.uid!!
    private val imageId = UUID.randomUUID().toString()

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
            if (uris != null) {
                if(imageList.size + uris.size > 10) {
                    Toast.makeText(context,"사진은 최대 10장까지 선택 가능합니다.",Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                val writeImageItems = uris.mapIndexed { index, uri ->
                    WriteImageItem(imageId, uri.toString())
                }
                imageList.addAll(uris)
                writeImageAdapter.setPostImageItemList(writeImageItems)
                binding.imageCount.text = imageCount.toString()
            } else {
                // uri 리스트에 값이 없을 경우
            }
        }

    override fun onResume() {
        super.onResume()

        val mainActivity = activity as MainActivity
        mainActivity.hideBottomNavigation(true)
    }

    override fun onPause() {
        super.onPause()

        val mainActivity = activity as MainActivity
        mainActivity.hideBottomNavigation(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPostWriteBinding.bind(view)
        writeImageAdapter = WriteImageAdapter(binding.imageCount)

        homePostAdapter = HomePostAdapter { post ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToPostDetailFragment(
                    post.writerId.orEmpty(), post.postId.orEmpty()
                )
            )
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.addImageButton.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.submitButton.setOnClickListener {
            if (binding.writePostPrice.text.toString().toInt() < 20000) {
                Toast.makeText(context, "판매가격을 20000원 이상으로 설정해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (imageList.isNotEmpty() && !binding.writePostTitle.text.isNullOrBlank()
                && !binding.writePostPrice.text.isNullOrBlank() && !binding.writePostContent.text.isNullOrBlank()
            ) {
                val imageUris = writeImageAdapter.imageList.map{ Uri.parse(it.imageUrl) } ?: return@setOnClickListener
                uploadImages(imageUris,
                    successHandler = {
                        uploadPost(it)
                    },
                    errorHandler = {
                        Toast.makeText(context, "이미지 업로드에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    })
            } else {
                Toast.makeText(context, "게시글 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imageRecyclerview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = writeImageAdapter
        }
    }

    private fun uploadImages(
        uris: List<Uri>,
        successHandler: (List<String>) -> Unit,
        errorHandler: (Throwable?) -> Unit
    ) {
        val uploadedUrls = mutableListOf<String>()

        fun uploadNextImage(index: Int) {
            if (index >= uris.size) {
                successHandler(uploadedUrls)
                return
            }

            val uri = uris[index]
            val imageId = UUID.randomUUID().toString() // 각 이미지마다 새로운 UUID 생성
            val fileName = "${imageId}.png"
            Firebase.storage.reference.child("posts/${postId}").child(fileName)
                .putFile(uri)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Firebase.storage.reference.child("posts/${postId}/${fileName}")
                            .downloadUrl
                            .addOnSuccessListener { url ->
                                uploadedUrls.add(url.toString())
                                uploadNextImage(index + 1) // 다음 이미지 업로드
                            }.addOnFailureListener { exception ->
                                errorHandler(exception)
                            }
                    } else {
                        task.exception?.printStackTrace()
                        errorHandler(task.exception)
                    }
                }
        }
        uploadNextImage(0)
    }

    private fun uploadPost(photoUrl: List<String>) {
        val addPostList = mutableListOf<PostItem>()

        Firebase.database.reference.child("Users").child(writerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var writerNickname = snapshot.child("userNickname").getValue(String::class.java)
                    var writerPhone = snapshot.child("userPhone").getValue(String::class.java)
                    var writerStar = snapshot.child("userStar").getValue(Double::class.java)

                    val post = PostItem(
                        postId = postId,
                        createdAt = System.currentTimeMillis(),
                        postImagesUrl = photoUrl,
                        postThumbnailUrl = photoUrl[0],
                        postTitle = binding.writePostTitle.text.toString(),
                        postPrice = binding.writePostPrice.text.toString() + " 원",
                        postContent = binding.writePostContent.text.toString(),
                        postStatus = true,
                        writerId = writerId,
                        writerNickname = writerNickname,
                        writerPhone = writerPhone,
                        writerStar = writerStar
                    )
                    Firebase.database.reference.child("Posts").child(postId).setValue(post)
                        .addOnSuccessListener {
                            if(Firebase.auth.currentUser?.uid.isNullOrEmpty())
                                return@addOnSuccessListener
                            addPostList.add(post)
                            homePostAdapter.submitList(addPostList)

                            findNavController().popBackStack()
                        }.addOnFailureListener {
                            Toast.makeText(context, "게시글 정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }
}