package net.osmand.plus.mapcontextmenu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.IndexConstants;
import net.osmand.data.Amenity;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.development.OsmandDevelopmentPlugin;
import net.osmand.plus.helpers.FileNameTranslationHelper;
import net.osmand.plus.wikivoyage.data.TravelArticle;
import net.osmand.util.Algorithms;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static net.osmand.plus.OsmandSettings.WikivoyageShowImages.OFF;

public class WikipediaDialogFragment extends DialogFragment {

	public static final String TAG = "WikipediaDialogFragment";

	private static final String HEADER_INNER = "<html><head>\n" +
			"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
			"<meta http-equiv=\"cleartype\" content=\"on\" />\n" +
			"<link href=\"article_style.css\" type=\"text/css\" rel=\"stylesheet\"/>\n" +
			"</head>";
	private static final String FOOTER_INNER = "</body></html>";

	private View mainView;
	private boolean darkMode;
	private Amenity amenity;
	private String lang;
	WebView contentWebView;
	public void setAmenity(Amenity amenity) {
		this.amenity = amenity;
	}

	public void setLanguage(String lang) {
		this.lang = lang;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OsmandApplication app = getMyApplication();
		darkMode = app.getDaynightHelper().isNightMode() || !app.getSettings().isLightContent();
		int themeId = darkMode ? R.style.OsmandDarkTheme : R.style.OsmandLightTheme;
		setStyle(STYLE_NO_FRAME, themeId);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = new Dialog(getContext(), getTheme());
		if (!getMyApplication().getSettings().DO_NOT_USE_ANIMATIONS.get()) {
			dialog.getWindow().getAttributes().windowAnimations = R.style.Animations_Alpha;
		}
		return dialog;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.wikipedia_dialog_fragment, container, false);

		mainView.setBackgroundColor(ContextCompat.getColor(getContext(), darkMode ? R.color.ctx_menu_bottom_view_bg_dark : R.color.ctx_menu_bottom_view_bg_light));

		AppBarLayout appBarLayout = (AppBarLayout) mainView.findViewById(R.id.app_bar);
		appBarLayout.setBackgroundColor(ContextCompat.getColor(getContext(), darkMode ? R.color.ctx_menu_buttons_bg_dark: R.color.ctx_menu_buttons_bg_light));

		int toolbarTextColor = ContextCompat.getColor(getContext(), R.color.dashboard_subheader_text_light);

		ImageButton backButton = (ImageButton) mainView.findViewById(R.id.back_button);
		backButton.setImageDrawable(getMyApplication().getIconsCache().getPaintedIcon(R.drawable.ic_arrow_back, toolbarTextColor));
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		TextView titleTextView = (TextView) mainView.findViewById(R.id.title_text_view);
		titleTextView.setTextColor(toolbarTextColor);

		ColorStateList buttonColorStateList = AndroidUtils.createPressedColorStateList(getContext(), darkMode,
				R.color.ctx_menu_controller_button_text_color_light_n, R.color.ctx_menu_controller_button_text_color_light_p,
				R.color.ctx_menu_controller_button_text_color_dark_n, R.color.ctx_menu_controller_button_text_color_dark_p);

		final TextView readFullArticleButton = (TextView) mainView.findViewById(R.id.read_full_article);

		readFullArticleButton.setBackgroundResource(darkMode ? R.drawable.bt_round_long_night : R.drawable.bt_round_long_day);
		readFullArticleButton.setTextColor(buttonColorStateList);
		int paddingLeft = (int) getResources().getDimension(R.dimen.wikipedia_button_left_padding);
		int paddingRight = (int) getResources().getDimension(R.dimen.dialog_content_margin);
		readFullArticleButton.setPadding(paddingLeft, 0, paddingRight, 0);
		readFullArticleButton.setCompoundDrawablesWithIntrinsicBounds(getIcon(R.drawable.ic_world_globe_dark), null, null, null);
		readFullArticleButton.setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.content_padding_small));

		final TextView selectLanguageTextView = mainView.findViewById(R.id.select_language_text_view);
		selectLanguageTextView.setTextColor(buttonColorStateList);
		selectLanguageTextView.setCompoundDrawablesWithIntrinsicBounds(getIcon(R.drawable.ic_action_map_language), null, null, null);
		selectLanguageTextView.setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.context_menu_padding_margin_small));
		selectLanguageTextView.setBackgroundResource(darkMode ? R.drawable.wikipedia_select_lang_bg_dark : R.drawable.wikipedia_select_lang_bg_light);
		contentWebView = (WebView) mainView.findViewById(R.id.content_web_view);
		WebSettings webSettings = contentWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		return mainView;
	}

	@NonNull
	private String getBaseUrl() {
		File wikivoyageDir = getMyApplication().getAppPath(IndexConstants.WIKIVOYAGE_INDEX_DIR);
		if (new File(wikivoyageDir, "article_style.css").exists()) {
			return "file://" + wikivoyageDir.getAbsolutePath() + "/";
		}
		return "file:///android_asset/";
	}

	@NonNull
	private String createHtmlContent(@NonNull String article) {
		StringBuilder sb = new StringBuilder(HEADER_INNER);

		String nightModeClass = darkMode ? " nightmode" : "";

		sb.append("<div class=\"main" + nightModeClass + "\">\n");
		sb.append(article);
		sb.append(FOOTER_INNER);
		return sb.toString();
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		populateWiki();
	}

	@Override
	public void onDestroyView() {
		Dialog dialog = getDialog();
		if (dialog != null) {
			dialog.setDismissMessage(null);
		}
		super.onDestroyView();
	}

	private void populateWiki() {
		if (amenity != null) {
			String preferredLanguage = lang;
			if (TextUtils.isEmpty(preferredLanguage)) {
				preferredLanguage = getMyApplication().getLanguage();
			}

			String lng = amenity.getContentLanguage("content", preferredLanguage, "en");
			if (Algorithms.isEmpty(lng)) {
				lng = "en";
			}

			final String langSelected = lng;
			final String title = amenity.getName(langSelected);
			((TextView) mainView.findViewById(R.id.title_text_view)).setText(title);

			mainView.findViewById(R.id.read_full_article).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					String article = "https://" + langSelected.toLowerCase() + ".wikipedia.org/wiki/" + title.replace(' ', '_');
					showFullArticle(getContext(), Uri.parse(article), darkMode);
				}
			});

			final TextView selectLanguageTextView = mainView.findViewById(R.id.select_language_text_view);
			selectLanguageTextView.setText(langSelected);
			selectLanguageTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					showPopupLangMenu(selectLanguageTextView, langSelected);
				}
			});

			String content = amenity.getDescription(langSelected);
			contentWebView.loadDataWithBaseURL(getBaseUrl(), createHtmlContent(content), "text/html", "UTF-8", null);
		}
	}

	public static void showFullArticle(Context context, Uri uri, boolean nightMode) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
					.setToolbarColor(ContextCompat.getColor(context, nightMode ? R.color.actionbar_dark_color : R.color.actionbar_light_color))
					.build();
			customTabsIntent.launchUrl(context, uri);
		} else {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(uri);
			context.startActivity(i);
		}
	}

	private void showPopupLangMenu(View view, final String langSelected) {
		final PopupMenu optionsMenu = new PopupMenu(getContext(), view, Gravity.RIGHT);
		Set<String> namesSet = new TreeSet<>();
		namesSet.addAll(amenity.getNames("content", "en"));
		namesSet.addAll(amenity.getNames("description", "en"));

		Map<String, String> names = new HashMap<>();
		for (String n : namesSet) {
			names.put(n, FileNameTranslationHelper.getVoiceName(getContext(), n));
		}
		String selectedLangName = names.get(langSelected);
		if (selectedLangName != null) {
			names.remove(langSelected);
		}
		Map<String, String> sortedNames = AndroidUtils.sortByValue(names);

		if (selectedLangName != null) {
			MenuItem item = optionsMenu.getMenu().add(selectedLangName);
			item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					setLanguage(langSelected);
					populateWiki();
					return true;
				}
			});
		}
		for (final Map.Entry<String, String> e : sortedNames.entrySet()) {
			MenuItem item = optionsMenu.getMenu().add(e.getValue());
			item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					setLanguage(e.getKey());
					populateWiki();
					return true;
				}
			});
		}
		optionsMenu.show();
	}

	private Drawable getIcon(int resId) {
		int colorId = darkMode ? R.color.ctx_menu_controller_button_text_color_dark_n : R.color.ctx_menu_controller_button_text_color_light_n;
		return getMyApplication().getIconsCache().getIcon(resId, colorId);
	}

	private OsmandApplication getMyApplication() {
		return (OsmandApplication) getActivity().getApplication();
	}

	public static boolean showInstance(AppCompatActivity activity, Amenity amenity, String lang) {
		try {
			if (!amenity.getType().isWiki()) {
				return false;
			}
			OsmandApplication app = (OsmandApplication) activity.getApplication();

			WikipediaDialogFragment wikipediaDialogFragment = new WikipediaDialogFragment();
			wikipediaDialogFragment.setAmenity(amenity);
			wikipediaDialogFragment.setLanguage(lang == null ?
					app.getSettings().MAP_PREFERRED_LOCALE.get() : lang);
			wikipediaDialogFragment.setRetainInstance(true);
			wikipediaDialogFragment.show(activity.getSupportFragmentManager(), TAG);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	public static boolean showInstance(AppCompatActivity activity, Amenity amenity) {
		return showInstance(activity, amenity, null);
	}
}
