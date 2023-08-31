/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import dev.sasikanth.rss.reader.CommonRes
import dev.sasikanth.rss.reader.components.ScrollToTopButton
import dev.sasikanth.rss.reader.home.ui.PostListItem
import dev.sasikanth.rss.reader.search.SearchEvent
import dev.sasikanth.rss.reader.search.SearchPresenter
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState

@Composable
internal fun SearchScreen(
  searchPresenter: SearchPresenter,
  openLink: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val state by searchPresenter.state.collectAsState()
  val listState = rememberLazyListState()
  val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
  val layoutDirection = LocalLayoutDirection.current

  Scaffold(
    modifier = modifier,
    topBar = {
      SearchBar(
        query = searchPresenter.searchQuery,
        onQueryChange = { searchPresenter.dispatch(SearchEvent.SearchQueryChanged(it)) },
        onBackClick = { searchPresenter.dispatch(SearchEvent.BackClicked) },
        onClearClick = { searchPresenter.dispatch(SearchEvent.SearchQueryChanged("")) }
      )
    },
    content = {
      Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
          contentPadding =
            PaddingValues(
              start = it.calculateStartPadding(layoutDirection),
              end = it.calculateEndPadding(layoutDirection),
              bottom = it.calculateBottomPadding() + 64.dp
            ),
          state = listState,
          modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
          itemsIndexed(state.searchResults) { index, post ->
            PostListItem(post) { openLink(post.link) }
            if (index != state.searchResults.lastIndex) {
              Divider(
                modifier = Modifier.fillParentMaxWidth().padding(horizontal = 24.dp),
                color = AppTheme.colorScheme.surfaceContainer
              )
            }
          }
        }

        ScrollToTopButton(
          visible = showScrollToTop,
          modifier =
            Modifier.windowInsetsPadding(WindowInsets.navigationBars)
              .padding(end = 24.dp, bottom = 24.dp)
        ) {
          listState.animateScrollToItem(0)
        }
      }
    },
    containerColor = Color.Unspecified,
    contentColor = Color.Unspecified
  )
}

@Composable
private fun SearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  onBackClick: () -> Unit,
  onClearClick: () -> Unit
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardState by keyboardVisibilityAsState()
  val focusManager = LocalFocusManager.current

  LaunchedEffect(keyboardState) {
    if (keyboardState == KeyboardState.Closed) {
      focusManager.clearFocus()
    }
  }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .windowInsetsPadding(WindowInsets.statusBars)
        .background(AppTheme.colorScheme.surface)
  ) {
    TextField(
      modifier = Modifier.fillMaxWidth().padding(all = 16.dp).focusRequester(focusRequester),
      value = query,
      onValueChange = onQueryChange,
      placeholder = {
        Text(
          stringResource(CommonRes.strings.search_hint),
          color = AppTheme.colorScheme.textEmphasisHigh,
          style = MaterialTheme.typography.bodyLarge
        )
      },
      leadingIcon = {
        IconButton(onClick = onBackClick) {
          Icon(
            Icons.Rounded.ArrowBack,
            contentDescription = null,
            tint = AppTheme.colorScheme.onSurface
          )
        }
      },
      trailingIcon = {
        if (query.isNotBlank()) {
          IconButton(
            onClick = {
              focusRequester.requestFocus()
              onClearClick()
            }
          ) {
            Icon(
              Icons.Rounded.Close,
              contentDescription = null,
              tint = AppTheme.colorScheme.onSurface
            )
          }
        }
      },
      shape = RoundedCornerShape(16.dp),
      singleLine = true,
      textStyle = MaterialTheme.typography.bodyLarge,
      colors =
        TextFieldDefaults.colors(
          focusedContainerColor = AppTheme.colorScheme.surfaceContainer,
          unfocusedContainerColor = AppTheme.colorScheme.surfaceContainer,
          focusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
          unfocusedIndicatorColor = Color.Unspecified,
          focusedIndicatorColor = Color.Unspecified,
          disabledIndicatorColor = Color.Unspecified,
          errorIndicatorColor = Color.Unspecified
        )
    )

    Divider(
      modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
      color = AppTheme.colorScheme.surfaceContainer
    )
  }
}