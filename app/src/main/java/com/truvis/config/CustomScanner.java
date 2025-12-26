package com.truvis.config;

import org.hibernate.boot.archive.internal.StandardArchiveDescriptorFactory;
import org.hibernate.boot.archive.scan.internal.StandardScanner;
import org.hibernate.boot.archive.scan.spi.ScanEnvironment;
import org.hibernate.boot.archive.scan.spi.ScanOptions;
import org.hibernate.boot.archive.scan.spi.ScanParameters;
import org.hibernate.boot.archive.scan.spi.ScanResult;

/**
 * Custom Hibernate Scanner
 * - timescale 패키지 제외
 */
public class CustomScanner extends StandardScanner {

    @Override
    public ScanResult scan(ScanEnvironment environment, ScanOptions options, ScanParameters parameters) {
        // 원래 스캔 결과 가져오기
        ScanResult originalResult = super.scan(environment, options, parameters);
        
        // timescale 패키지 필터링
        return new FilteredScanResult(originalResult);
    }
    
    private static class FilteredScanResult implements ScanResult {
        private final ScanResult delegate;
        
        public FilteredScanResult(ScanResult delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public java.util.Set<org.hibernate.boot.archive.scan.spi.ClassDescriptor> getLocatedClasses() {
            return delegate.getLocatedClasses().stream()
                    .filter(classDescriptor -> !classDescriptor.getName().contains(".timescale."))
                    .collect(java.util.stream.Collectors.toSet());
        }
        
        @Override
        public java.util.Set<org.hibernate.boot.archive.scan.spi.PackageDescriptor> getLocatedPackages() {
            return delegate.getLocatedPackages();
        }
        
        @Override
        public java.util.Set<org.hibernate.boot.archive.scan.spi.MappingFileDescriptor> getLocatedMappingFiles() {
            return delegate.getLocatedMappingFiles();
        }
    }
}



