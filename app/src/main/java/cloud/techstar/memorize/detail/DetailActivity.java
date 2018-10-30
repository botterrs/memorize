package cloud.techstar.memorize.detail;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cloud.techstar.memorize.AppMain;
import cloud.techstar.memorize.Injection;
import cloud.techstar.memorize.R;
import cloud.techstar.memorize.database.Words;

public class DetailActivity extends AppCompatActivity implements DetailContract.View{

    private ImageButton favBtn;
    private TextView meaningMn;
    private TextView headKanji;
    private TextView headHiragana;
    private TextView meaning;
    private TextView partOfSpeech;
    private TextView level;
    private TextView kanji;

    private DetailContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        headKanji = (TextView) findViewById(R.id.header_kanji);
        headHiragana = (TextView) findViewById(R.id.header_hiragana);
        meaning = (TextView) findViewById(R.id.detail_meaning);
        meaningMn = (TextView) findViewById(R.id.detail_meaning_mn);
        partOfSpeech = (TextView) findViewById(R.id.detail_part_of);
        level = (TextView) findViewById(R.id.detail_level);
        kanji = (TextView) findViewById(R.id.detail_kanji);

        ImageButton backBtn = findViewById(R.id.back);
        favBtn = findViewById(R.id.btnFav);
        Intent intent = getIntent();

        new DetailPresenter((Words) intent.getSerializableExtra("word_detail"),
                Injection.provideWordsRepository(getApplicationContext()),
                this);

        presenter.init();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.favoriteWord();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void setPresenter(DetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(AppMain.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setLoadingIndicator(boolean active) {

    }

    @Override
    public void showMissingWord() {

    }

    @Override
    public void setData(Words word) {
        try {
            headKanji.setText(word.getKanji());
            headHiragana.setText(word.getCharacter());
            meaning.setText("\u2022 ".concat(word.getMeaning().get(0)));
            meaningMn.setText("\u2022 ".concat(word.getMeaningMon().get(0)));
            partOfSpeech.setText(word.getPartOfSpeech().get(0));
            level.setText(word.getLevel().get(0));
            kanji.setText(word.getKanji());
            if (word.isFavorite())
                favBtn.setImageResource(R.drawable.ic_favorite_full);
        } catch (Exception ex){
            showToast("Алдаа :"+ex);
        }
    }

    @Override
    public void showFavorite(boolean isFav) {
        if (isFav)
            favBtn.setImageResource(R.drawable.ic_favorite_full);
        else
            favBtn.setImageResource(R.drawable.ic_favorite);
    }

}
