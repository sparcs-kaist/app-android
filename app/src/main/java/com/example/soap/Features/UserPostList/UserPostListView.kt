package com.example.soap.Features.UserPostList


//@Composable
//fun UserPostListScreen(
//    user: AraPostAuthor,
//    viewModel: UserPostListViewModel = hiltViewModel()
//) {
//    val state by viewModel.state.collectAsState()
//    val posts by viewModel.posts.collectAsState()
//    LaunchedEffect(Unit) {
//        viewModel.bind()
//        viewModel.fetchInitialPosts()
//    }
//
//    when (state) {
//        is UserPostListViewModel.ViewState.Loading -> {
//            PostList(posts = null)
//        }
//
//        is UserPostListViewModel.ViewState.Loaded -> {
//            val loadedPosts = (state as UserPostListViewModel.ViewState.Loaded).posts
//            PostList(
//                posts = loadedPosts,
//                onPostClick = { post ->
//                    PostView(post = post)
//                    viewModel.refreshItem(post.id)
//                },
//                onRefresh = { viewModel.fetchInitialPosts() },
//                onLoadMore = { viewModel.loadNextPage() }
//            )
//        }
//
//        is UserPostListViewModel.ViewState.Error -> {
//            val message = (state as UserPostListViewModel.ViewState.Error).message
//            //TODO - Error View
//        }
//    }
//}

//TODO = Post, PostCompose, PostList, PostTranslation