package com.ekodomo.urlchecker.modules.list;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ekodomo.urlchecker.R;
import com.ekodomo.urlchecker.activities.ModulesActivity;
import com.ekodomo.urlchecker.dialogs.MainDialog;
import com.ekodomo.urlchecker.modules.AModuleConfig;
import com.ekodomo.urlchecker.modules.AModuleData;
import com.ekodomo.urlchecker.modules.AModuleDialog;
import com.ekodomo.urlchecker.url.UrlData;
import com.ekodomo.urlchecker.utilities.generics.GenericPref.StringPref;
import com.ekodomo.urlchecker.utilities.methods.JavaUtils.Function;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class BlockerModule extends AModuleData {

    @Override
    public String getId() {
        return "blocker";
    }

    @Override
    public int getName() {
        return R.string.mBlocker_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new BlockerDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new BlockerConfig(cntx);
    }
}

class BlockerConfig extends AModuleConfig {

    public BlockerConfig(ModulesActivity activity) {
        super(activity);
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_blocker;
    }

    @Override
    public void onInitialize(View views) {
        new StringPref("blocker_rules", "", getActivity()).attachToEditText(views.findViewById(R.id.rules));
    }
}

class BlockerDialog extends AModuleDialog {

    private final StringPref rulesPref;

    public BlockerDialog(MainDialog dialog) {
        super(dialog);
        rulesPref = new StringPref("blocker_rules", "", dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_blocker;
    }

    @Override
    public void onInitialize(View views) {
        // Just static UI when blocked
    }

    @Override
    public void onModifyUrl(UrlData urlData, Function<UrlData, Boolean> setNewUrl) {
        String url = urlData.url;
        String rulesStr = rulesPref.get();
        if (rulesStr.isEmpty()) {
            setVisibility(false);
            return;
        }

        String host = "";
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException ignored) {
        }

        String[] rules = rulesStr.split("\n");
        boolean blocked = false;

        for (String rule : rules) {
            rule = rule.trim();
            if (rule.isEmpty()) continue;

            // Convert wildcard to regex
            String regex = rule.replace(".", "\\.").replace("+", "\\+").replace("[", "\\[").replace("]", "\\]").replace("{", "\\{").replace("}", "\\}").replace("(", "\\(").replace(")", "\\)").replace("^", "\\^").replace("$", "\\$").replace("|", "\\|").replace("?", ".").replace("*", ".*");

            try {
                Pattern pattern = Pattern.compile("^" + regex + "$");
                if (pattern.matcher(url).matches() || (!host.isEmpty() && pattern.matcher(host).matches())) {
                    blocked = true;
                    break;
                }
            } catch (PatternSyntaxException ignored) {
                // Ignore invalid patterns
            }
        }

        if (blocked) {
            Toast.makeText(getActivity(), R.string.mBlocker_blocked, Toast.LENGTH_LONG).show();
            getActivity().finish();
            setNewUrl.apply(new UrlData(""));
        } else {
            setVisibility(false);
        }
    }
}
