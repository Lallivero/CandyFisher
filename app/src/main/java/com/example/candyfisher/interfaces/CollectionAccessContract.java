package com.example.candyfisher.interfaces;

import com.example.candyfisher.models.CollectionListData;
@Deprecated
public interface CollectionAccessContract {
    interface CollectionView {
        void initialiseView();

    }

    interface CollectionPresenter {
        void initialisePresenter();

        void swapCollected(int index);

        int getCollectionSize();

        String getItemDescription(int index);

        Boolean getCollectedStatus(int index);



        int getImageId(int index);

        void onClick(int index);
    }
}
