package com.example.Simple_logic.Service.search;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.Service.thanhphan.CayThanhPhanCache;

@Service
public class SearchIndexSyncService {

    private final CayThanhPhanCache cayThanhPhanCache;
    private final Bm25Service bm25Service;

    public SearchIndexSyncService(
            CayThanhPhanCache cayThanhPhanCache,
            Bm25Service bm25Service) {
        this.cayThanhPhanCache = cayThanhPhanCache;
        this.bm25Service = bm25Service;
    }

    /** Nạp lại cache cây thành phần và dựng lại chỉ mục BM25 trong RAM. */
    public synchronized void reloadAll() {
        cayThanhPhanCache.reload();
        bm25Service.rebuild();
    }
}
