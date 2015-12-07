package com.futurice.hereandnow.card;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.futurice.cascade.util.Origin;
import com.futurice.cascade.util.RCLog;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.singleton.ModelSingleton;
import com.futurice.hereandnow.singleton.ServiceSingleton;
import com.futurice.hereandnow.utils.FlavorUtils;
import com.futurice.scampiclient.items.Peer;
import com.futurice.scampiclient.utils.ArrayUtils;

import java.util.Date;
import java.util.List;

/**
 * Base class for cards.
 *
 * @author teemuk
 */
public abstract class BaseCard extends Origin implements ICard {

    private static final String TAG = BaseCard.class.getSimpleName();

    @NonNull
    protected final Context context;
    protected final int layoutResource;

    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final String name;
    private final long uid;
    @NonNull
    private boolean flagged = false;
    private String author;
    private String authorId;
    private Date date;

    protected BaseCard(
            @NonNull final String name,
            final long uid,
            @NonNull final Context context,
            final int layoutResource) {
        this.name = name;
        this.uid = uid;
        this.context = context;
        this.layoutResource = layoutResource;

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * A new card view is being created by the list model
     *
     * @param parentView
     * @return
     */
    @Override
    @NonNull

    public View getView(@NonNull final ViewGroup parentView) {
        // Inflate the new view
        final View newView = this.inflateView(this.layoutResource, parentView);
        updateView(newView);

        return newView;
    }

    /**
     * An existing View is being recycled by the list model
     *
     * @param view           view whose contents to update
     */
    @Override
    public void updateView(@NonNull final View view) {
        RCLog.d(this, "updateView");
        try {
            if (FlavorUtils.isSuperuserBuild && this.flagged) {
                // Show delete button in red for flagged cards
                ImageView cardDeleteButton = (ImageView) view.findViewById(R.id.card_delete_button);
                cardDeleteButton.setColorFilter(Color.rgb(255, 0, 0));
            }

            LinearLayout cardBase = (LinearLayout) view.findViewById(R.id.card_base);
            LinearLayout topicButtonBar = (LinearLayout) view.findViewById(R.id.topic_button_bar);
            ImageView cardLikeButton = (ImageView) view.findViewById(R.id.card_like_button);
            ImageView cardCommentButton = (ImageView) view.findViewById(R.id.card_comment_button);
            ImageView cardUserButton = (ImageView) view.findViewById(R.id.card_user_button);
            ImageView cardDeleteButton = (ImageView) view.findViewById(R.id.card_delete_button);
            ImageView cardFlagButton = (ImageView) view.findViewById(R.id.card_flag_button);

            cardLikeButton.setOnClickListener(v -> likeButtonClicked(v));
            cardCommentButton.setOnClickListener(this::commentButtonClicked);
            cardUserButton.setOnClickListener(this::userButtonClicked);
            cardDeleteButton.setOnClickListener(this::deleteCardButtonClicked);
            cardFlagButton.setOnClickListener(this::flagCardButtonClicked);
        } catch (Exception e) {
            Log.e(TAG, "Elements not present in the people card");
        }
    }

    protected boolean userAlreadyLikes() {
        return ArrayUtils.valueExists(ModelSingleton.instance().myLikes.get(), getUid());
    }

    private boolean cardAlreadyDeleted() {
        return ArrayUtils.valueExists(ModelSingleton.instance().deletedCards.get(), getUid());
    }

    private boolean cardAlreadyFlagged() {
        return ArrayUtils.valueExists(ModelSingleton.instance().flaggedCards.get(), getUid());
    }

    private void likeButtonClicked(@NonNull final View view) {
        final Toast toast = makeToast(R.layout.basic_toast, Toast.LENGTH_SHORT);

        ImageView cardLikeButton = (ImageView) view.findViewById(R.id.card_like_button);
        ImageView toastImage = (ImageView) toast.getView().findViewById(R.id.toast_image);
        TextView toastTextView = (TextView) toast.getView().findViewById(R.id.toast_text);

        Drawable likeDrawable;
        String toastText;

        if (!userAlreadyLikes()) {
            addLocalUserLike();
            likeDrawable = ContextCompat.getDrawable(context, R.drawable.card_navbar_liked);
            toastText = context.getString(R.string.card_like_toast_message);
        } else {
            deleteLocalUserLike();
            likeDrawable = ContextCompat.getDrawable(context, R.drawable.card_navbar_like);
            toastText = context.getString(R.string.card_unlike_toast_message);
        }

        cardLikeButton.setImageDrawable(likeDrawable);
        toastImage.setImageDrawable(likeDrawable);
        toastTextView.setText(toastText);

        toast.show();
    }

    private void commentButtonClicked(@NonNull final View view) {
        final Context context = view.getContext();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View dialogView = View.inflate(context, R.layout.comment_box, null);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.gen_ok, (dialogInterface, i) -> {
            EditText commentEditText = (EditText) dialogView.findViewById(R.id.comment_text);
            String commentText = commentEditText.getText().toString();
            addLocalUserComment(commentText);
        });
        builder.setNegativeButton(R.string.gen_cancel, null);
        builder.create().show();
    }

    private void addLocalUserComment(final String commentString) {
        if (TextUtils.isEmpty(commentString)) {
            return;
        }

        final Comment comment = new Comment(
                this.getUid(),
                ModelSingleton.instance().myTag.get(),
                ModelSingleton.instance().myIdTag.get(),
                commentString.trim());

        ServiceSingleton.instance()
                .peerDiscoveryService()
                .localUserCommentsACardAsync(comment.toJSONString())
                .then(() -> {
                    ModelSingleton.instance().myComments.set(
                            ServiceSingleton.instance().peerDiscoveryService().getLocalUserComments());
                })
                .fork();
    }

    protected void deleteLocalUserComment(Comment comment) {
        if (comment == null) {
            return;
        }

        ServiceSingleton.instance().peerDiscoveryService()
                .localUserRemovesCommentAsync(comment.toJSONString())
                .then(() -> {
                    ModelSingleton.instance().myComments.set(
                            ServiceSingleton.instance().peerDiscoveryService().getLocalUserComments());
                })
                .fork();
    }

    private void deleteCardButtonClicked(@NonNull final View view) {
        final Context context = view.getContext();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(R.string.topic_delete_confirmation)
                .setPositiveButton(R.string.gen_ok, (dialogInterface, i) -> deleteCard())
                .setNegativeButton(R.string.gen_cancel, (dialogInterface, i) -> {
                });
        builder.create().show();
    }

    private void flagCardButtonClicked(@NonNull final View view) {
        final Context context = view.getContext();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(R.string.topic_flag_confirmation)
                .setPositiveButton(R.string.gen_ok, (dialogInterface, i) -> flagCard())
                .setNegativeButton(R.string.gen_cancel, (dialogInterface, i) -> {
                });
        builder.create().show();
    }

    private void addLocalUserLike() {
        if (!userAlreadyLikes()) {
            ServiceSingleton.instance()
                    .peerDiscoveryService()
                    .localUserLikesACardAsync(this.getUid())
                    .then(() -> {
                        ModelSingleton.instance().myLikes.set(
                                ServiceSingleton.instance().peerDiscoveryService().getLocalUserLikes());
                    })
                    .fork();
        }
    }

    private void deleteLocalUserLike() {
        if (userAlreadyLikes()) {
            ServiceSingleton.instance()
                    .peerDiscoveryService()
                    .localUserUnlikesACardAsync(this.getUid())
                    .then(() -> {
                        ModelSingleton.instance().myLikes.set(
                                ServiceSingleton.instance().peerDiscoveryService().getLocalUserLikes());
                    })
                    .fork();
        }
    }

    private void deleteCard() {
        if (!cardAlreadyDeleted()) {
            ServiceSingleton.instance()
                    .peerDiscoveryService()
                    .localUserDeletesACardAsync(getUid())
                    .then(() -> ModelSingleton.instance().deleteCard(getUid()))
                    .fork();
        }
    }

    private void flagCard() {
        if (!cardAlreadyFlagged()) {
            ServiceSingleton.instance()
                    .peerDiscoveryService()
                    .localUserFlagsACardAsync(getUid())
                    .then(() -> ModelSingleton.instance().flagCard(getUid()))
                    .fork();
        }
    }

    protected void userButtonClicked(View view) {
        ModelSingleton modelSingleton = ModelSingleton.instance();
        List<Peer> peerModel = modelSingleton.getPeerModel();

        for (final Peer p : peerModel) {
            Log.d("BaseCard", "authorId=" + authorId + ", idTag=" + p.idTag);
            if (authorId != null && authorId.equals(p.idTag)) {
                displayAuthorDialog(p.tag.get(), p.aboutMe.get());
                return;
            }
        }

        String userNotFound = context.getResources().getString(R.string.people_user_not_found);
        Toast.makeText(context, String.format(userNotFound, author), Toast.LENGTH_LONG).show();
    }

    public void displayAuthorDialog(String name, String aboutMe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View authorLayout = inflater.inflate(R.layout.peer_card_layout, null);
        ((TextView) authorLayout.findViewById(R.id.peer_tag_text)).setText(name);
        ((TextView) authorLayout.findViewById(R.id.peer_about_me_text)).setText(aboutMe);

        builder.setView(authorLayout);
        builder.setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> {
                });
        builder.create().show();
    }

    /**
     * Specify the layout, customize what comes back if you like, subscribeTarget .show() the Toast
     *
     * @param layoutResource
     * @param toastDuration
     */
    @NonNull

    protected Toast makeToast(final int layoutResource, final int toastDuration) {
        final View view = inflater.inflate(layoutResource, null);
        final Toast toast = new Toast(context);
        toast.setView(view);
        toast.setDuration(toastDuration);

        return toast;
    }

    @NonNull

    protected View inflateView(final int resource, @NonNull final ViewGroup parentView) {
        return this.inflater.inflate(resource, parentView, false);
    }

    @Override // ICard
    public long getUid() {
        return uid;
    }

    @Override // INamed
    @NonNull

    public String getName() {
        return this.name;
    }

    @NonNull

    public String getAuthor() {
        if (author == null) {
            return context.getResources().getString(R.string.people_tag_anonymous);
        }

        return author;
    }

    public void setAuthor(@NonNull final String author, @NonNull final String authorId) {
        this.author = author;
        this.authorId = authorId;
    }

    @NonNull

    public Date getDate() {
        if (date == null) {
            //FIXME set date=newDate() ?
            return new Date();
        }

        return date;
    }

    public void setDate(@NonNull final Date date) {
        this.date = date;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(final boolean flagged) {
        this.flagged = flagged;
    }
}
