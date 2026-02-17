package com.axiom.v21.infrastructure;

import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Custom RocksDB configuration to prevent OOMs in containerized environments.
 * Sets strict limits on memory usage.
 */
public class AxiomRocksDBConfig implements RocksDBConfigSetter {

    private static final Logger logger = LoggerFactory.getLogger(AxiomRocksDBConfig.class);

    // 64MB Block Cache provided to the BlockBasedTableConfig
    private static final long BLOCK_CACHE_SIZE = 64 * 1024 * 1024L;

    // 16MB Write Buffer Size (Memtable)
    private static final long WRITE_BUFFER_SIZE = 16 * 1024 * 1024L;

    @Override
    public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
        logger.info("Configuring RocksDB for store: {}", storeName);

        BlockBasedTableConfig tableConfig = new org.rocksdb.BlockBasedTableConfig();
        tableConfig.setBlockCacheSize(BLOCK_CACHE_SIZE);
        tableConfig.setBlockSize(4 * 1024L); // 4KB block size
        tableConfig.setCacheIndexAndFilterBlocks(true); // Cache index and filter blocks in block cache

        options.setTableFormatConfig(tableConfig);
        options.setWriteBufferSize(WRITE_BUFFER_SIZE);
        options.setMaxWriteBufferNumber(2); // Max 2 memtables
        options.setMinWriteBufferNumberToMerge(1);

        // Optimize for SSD
        options.setBytesPerSync(1024 * 1024);
    }

    @Override
    public void close(final String storeName, final Options options) {
        // No-op
    }
}
