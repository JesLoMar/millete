package com.puntomartinez.millete.shared.infrastructure.nativeimage;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import java.util.Map;

@TargetClass(className = "org.hibernate.bytecode.internal.BytecodeProviderInitiator")
final class Target_BytecodeProviderInitiator {

    @Substitute
    public BytecodeProvider initiateService(
            Map<String, Object> configurationValues,
            ServiceRegistryImplementor registry) {

        return new org.hibernate.bytecode.internal.none.BytecodeProviderImpl();
    }
}