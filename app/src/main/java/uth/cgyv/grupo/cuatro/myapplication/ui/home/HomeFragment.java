package uth.cgyv.grupo.cuatro.myapplication.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import uth.cgyv.grupo.cuatro.myapplication.R;
import uth.cgyv.grupo.cuatro.myapplication.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        LinearLayout containerLayout = root.findViewById(R.id.containerLayout);

        addSection(inflater, containerLayout, R.string.title_login, R.string.desc_login);
        addSection(inflater, containerLayout, R.string.menu_gallery, R.string.desc_detection_basic);
        addSection(inflater, containerLayout, R.string.menu_slideshow, R.string.desc_detection_smile);
        addSection(inflater, containerLayout, R.string.menu_face_landmarks, R.string.desc_landmarks);
        addSection(inflater, containerLayout, R.string.menu_face_contour, R.string.desc_face_contour);
        addSection(inflater, containerLayout, R.string.btn_head_angle, R.string.desc_head_angle);
        addSection(inflater, containerLayout, R.string.btn_eye_state, R.string.desc_eyes_open);
        addSection(inflater, containerLayout, R.string.menu_face_emotions, R.string.desc_face_emotions);
        addSection(inflater, containerLayout, R.string.menu_filtros, R.string.desc_filters);

        return root;
    }

    private void addSection(LayoutInflater inflater, LinearLayout parent, int titleResId, int descResId) {
        View card = inflater.inflate(R.layout.container_section, parent, false);
        ((TextView) card.findViewById(R.id.titleText)).setText(getString(titleResId));
        ((TextView) card.findViewById(R.id.descriptionText)).setText(getString(descResId));
        parent.addView(card);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}