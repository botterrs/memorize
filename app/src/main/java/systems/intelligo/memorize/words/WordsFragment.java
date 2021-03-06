package systems.intelligo.memorize.words;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import systems.intelligo.memorize.AppMain;
import systems.intelligo.memorize.Injection;
import systems.intelligo.memorize.R;
import systems.intelligo.memorize.database.entity.Words;
import systems.intelligo.memorize.detail.DetailActivity;

import static com.google.common.base.Preconditions.checkNotNull;

public class WordsFragment extends Fragment implements WordsContract.View {

    private MaterialSearchBar searchBar;

    private WordsContract.Presenter presenter;
    private WordsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> suggestList = new ArrayList<>();

    private TextView filterText;

    public WordsFragment() {
    }

    public static android.app.Fragment newInstance() {
        WordsFragment fragment = new WordsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new WordsAdapter(new ArrayList<Words>(0), R.layout.item_word_list);

        new WordsPresenter(Injection.provideWordsRepository(AppMain.getContext()), this);

        presenter.init();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_words, container, false);

        filterText = root.findViewById(R.id.filter_title);
        searchBar = root.findViewById(R.id.searchBar);
        SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadWords(false);
            }
        });

        searchBar.setMaxSuggestionCount(2);
        searchBar.setSpeechMode(true);
        searchBar.setHint("Хайх үгээ оруул..");

        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("LOG_TAG", getClass().getSimpleName() + " text changed " + searchBar.getText());
                // send the entered text to our filter and let it manage everything
                List<String> suggest = new ArrayList<>();
                for (String search : suggestList) {
                    if (search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                searchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
//                if (!enabled)

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                presenter.search(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                switch (buttonCode) {
                    case MaterialSearchBar.BUTTON_NAVIGATION:

                        break;
                    case MaterialSearchBar.BUTTON_SPEECH:

                        break;

                    case MaterialSearchBar.BUTTON_BACK:
                        searchBar.disableSearch();

                }
            }
        });

        mRecyclerView = root.findViewById(R.id.word_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(AppMain.getContext());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        Spinner sortSpinner = root.findViewById(R.id.sort);
        Spinner viewSpinner = root.findViewById(R.id.view);

        List<String> sorts = new LinkedList<>(Arrays.asList("Recently", "Active", "All", "Translate"));

        List<String> views = new LinkedList<>(Arrays.asList("List", "Grid", "Card"));

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(AppMain.getContext(),
                R.layout.spinner_item, sorts);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> viewAdapter = new ArrayAdapter<String>(AppMain.getContext(),
                R.layout.spinner_item, views);
        viewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sortSpinner.setAdapter(sortAdapter);
        viewSpinner.setAdapter(viewAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                com.orhanobut.logger.Logger.e("Selected position :" + position);
                switch (position) {
                    case 0:
                        presenter.setFilterType(WordFilterType.RECENTLY);
                        break;
                    case 1:
                        presenter.setFilterType(WordFilterType.ACTIVE_WORDS);
                        break;
                    case 2:
                        presenter.setFilterType(WordFilterType.ALL_WORDS);
                        break;
                    case 3:
                        presenter.setFilterType(WordFilterType.NOT_TRANSLATE);
                        break;
                    default:
                        presenter.setFilterType(WordFilterType.RECENTLY);
                        break;
                }
                presenter.loadWords(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        viewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mAdapter = new WordsAdapter(new ArrayList<Words>(0), R.layout.item_word_list);
                        mLayoutManager = new LinearLayoutManager(AppMain.getContext());
                        mRecyclerView.setLayoutManager(mLayoutManager);
                        mRecyclerView.setAdapter(mAdapter);
                        break;
                    case 1:
                        mAdapter = new WordsAdapter(new ArrayList<Words>(0), R.layout.item_word_grid);
                        mLayoutManager = new GridLayoutManager(AppMain.getContext(), 2);
                        mRecyclerView.setLayoutManager(mLayoutManager);
                        mRecyclerView.setAdapter(mAdapter);
                        break;
                    case 2:
                        presenter.setViewType(WordViewType.CARD);
                        break;
                    default:
                        presenter.setViewType(WordViewType.LIST);
                        break;
                }
                presenter.loadWords(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return root;
    }

    @Override
    public void setPresenter(WordsContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(AppMain.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setLoadingIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout refreshLayout = getView().findViewById(R.id.swipe_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(active);
            }
        });
    }

    @Override
    public void showWords(List<Words> words) {
        mAdapter.replaceData(words);
    }

    @Override
    public void showWordDetail(Words word) {
        Intent intent = new Intent(AppMain.getContext(), DetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("word_detail", word);
        suggestList.add(word.getCharacter());
        AppMain.getContext().startActivity(intent);
    }

    @Override
    public void showLoadingWordsError() {

    }

    @Override
    public void showNoWords() {

    }

    @Override
    public boolean isActive() {
        return false;
    }


    private class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.ViewHolder> {
        private List<Words> words;
        private final int resource;

        public WordsAdapter(List<Words> words, int resource) {
            this.words = words;
            this.resource = resource;
        }

        public void replaceData(List<Words> words) {
            setList(words);
            notifyDataSetChanged();
        }

        private void setList(List<Words> words) {
            this.words = checkNotNull(words);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView kanjiText;
            private TextView characterText;

            private ViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);
                kanjiText = v.findViewById(R.id.kanji_text);
                characterText = v.findViewById(R.id.character_text);
            }

            @Override
            public void onClick(View view) {
                presenter.openWordDetails(words.get(this.getAdapterPosition()));
            }
        }

        @Override
        public WordsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(resource, parent, false);
            WordsAdapter.ViewHolder vh = new WordsAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final WordsAdapter.ViewHolder holder, final int position) {
            holder.kanjiText.setText(words.get(position).getKanji());
            holder.characterText.setText(words.get(position).getCharacter());
            if (words.get(position).getIsLocal()) {
                holder.kanjiText.setTextColor(getResources().getColor(R.color.chartGreen));
                holder.characterText.setTextColor(getResources().getColor(R.color.chartGreen));
            }
        }

        @Override
        public int getItemCount() {
            return words.size();
        }
    }
}
